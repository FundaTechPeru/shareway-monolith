package com.fundatechperu.shareway.booking.infrastructure.integration;

import com.fundatechperu.shareway.booking.domain.service.PaymentsPort;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationFailedEvent;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationSucceededEvent;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link PaymentsPort} for the extracted Payments &amp; Settlement service at {@code
 * app.services.payments-settlement.url}. Both calls run once the booking transaction commits, so a
 * failure never rolls the booking back.
 *
 * <p>The internal authorization endpoint needs a card, which Booking &amp; Pricing does not know:
 * the adapter lists the active cards of the passenger with the passenger's own token ({@code GET
 * /api/v1/payment-methods}) and uses the first one, as the monolith did. The answer becomes the
 * same {@link PaymentAuthorizationSucceededEvent} or {@link PaymentAuthorizationFailedEvent} that
 * moves the booking saga.
 */
@Component
public class HttpPaymentsAdapter implements PaymentsPort {

  static final String API_KEY_HEADER = "X-Internal-Api-Key";

  private static final Logger log = LoggerFactory.getLogger(HttpPaymentsAdapter.class);

  private final RestClient restClient;
  private final String apiKey;
  private final ApplicationEventPublisher publisher;
  private final TransactionTemplate newTransaction;
  private final Clock clock;

  public HttpPaymentsAdapter(
      @Value("${app.services.payments-settlement.url}") String baseUrl,
      @Value("${app.services.payments-settlement.api-key:}") String apiKey,
      @Value("${app.internal.api-key}") String internalApiKey,
      @Value("${app.services.timeout-ms:3000}") long timeoutMs,
      ApplicationEventPublisher publisher,
      PlatformTransactionManager transactionManager,
      Clock clock) {
    Duration timeout = Duration.ofMillis(timeoutMs);
    JdkClientHttpRequestFactory requestFactory =
        new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(timeout).build());
    requestFactory.setReadTimeout(timeout);
    this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    this.apiKey = apiKey.isBlank() ? internalApiKey : apiKey;
    this.publisher = publisher;
    this.newTransaction = new TransactionTemplate(transactionManager);
    this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.clock = clock;
  }

  @Override
  public void requestAuthorization(
      UUID bookingId, UUID passengerId, BigDecimal amount, String currency) {
    String authorization = currentAuthorizationHeader();
    afterCommit(
        () -> {
          Object result = authorize(bookingId, passengerId, amount, currency, authorization);
          newTransaction.executeWithoutResult(status -> publisher.publishEvent(result));
        });
  }

  @Override
  public void notifyCancellation(
      UUID bookingId,
      UUID passengerId,
      String reason,
      Instant cancelledAt,
      Instant scheduledDepartureAt) {
    afterCommit(
        () -> {
          try {
            restClient
                .post()
                .uri("/api/v1/internal/events/booking-cancelled")
                .header(API_KEY_HEADER, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    new BookingCancelledRequest(
                        bookingId, toCancellationReason(reason), cancelledAt, scheduledDepartureAt))
                .retrieve()
                .toBodilessEntity();
          } catch (RestClientResponseException ex) {
            if (!ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)
                && !ex.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
              throw ex;
            }
            // A booking whose payment never got authorized has nothing to void or charge.
            log.info("Cancellation of booking {} ignored: {}", bookingId, messageOf(ex));
          }
        });
  }

  private Object authorize(
      UUID bookingId, UUID passengerId, BigDecimal amount, String currency, String authorization) {
    if (authorization == null) {
      return failed(bookingId, "No passenger token to choose a payment method");
    }
    try {
      List<Card> cards =
          restClient
              .get()
              .uri("/api/v1/payment-methods")
              .header(HttpHeaders.AUTHORIZATION, authorization)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Card>>() {});
      if (cards == null || cards.isEmpty()) {
        return failed(bookingId, "The passenger has no active payment method");
      }
      Payment payment =
          restClient
              .post()
              .uri("/api/v1/internal/payments/authorizations")
              .header(API_KEY_HEADER, apiKey)
              .contentType(MediaType.APPLICATION_JSON)
              .body(
                  new AuthorizationRequest(
                      bookingId, passengerId, cards.get(0).id(), amount, currency))
              .retrieve()
              .body(Payment.class);
      if (payment == null || !"AUTHORIZED".equals(payment.status())) {
        return failed(
            bookingId, "Payment not authorized: " + (payment == null ? null : payment.status()));
      }
      return new PaymentAuthorizationSucceededEvent(bookingId, payment.id(), clock.instant());
    } catch (RestClientResponseException ex) {
      return failed(bookingId, messageOf(ex));
    } catch (RuntimeException ex) {
      return failed(bookingId, "Payments & Settlement is not available: " + ex.getMessage());
    }
  }

  private PaymentAuthorizationFailedEvent failed(UUID bookingId, String reason) {
    log.warn("Payment of booking {} not authorized: {}", bookingId, reason);
    return new PaymentAuthorizationFailedEvent(bookingId, reason, clock.instant());
  }

  /**
   * Runs the action once the current transaction commits, or right away without one. Errors are
   * only logged: the booking is already committed and the saga timeout covers a lost answer.
   */
  private static void afterCommit(Runnable action) {
    Runnable guarded =
        () -> {
          try {
            action.run();
          } catch (RuntimeException ex) {
            log.warn("Call to Payments & Settlement failed: {}", ex.getMessage());
          }
        };
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              guarded.run();
            }
          });
    } else {
      guarded.run();
    }
  }

  private static String currentAuthorizationHeader() {
    if (RequestContextHolder.getRequestAttributes()
        instanceof ServletRequestAttributes attributes) {
      return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
    return null;
  }

  private static String messageOf(RestClientResponseException ex) {
    try {
      ErrorBody body = ex.getResponseBodyAs(ErrorBody.class);
      if (body != null && body.message() != null) {
        return body.message();
      }
    } catch (RuntimeException ignored) {
      // Not the error body of the service: fall back to the status.
    }
    return "Payments & Settlement answered " + ex.getStatusCode().value();
  }

  /**
   * Payments &amp; Settlement only knows the reasons it may penalize; platform reasons (payment
   * timeout or failure) void the authorization without cost, as NO_DRIVER_AVAILABLE does.
   */
  private static String toCancellationReason(String reason) {
    return switch (reason) {
      case "PASSENGER_CANCELLED", "NO_SHOW" -> reason;
      default -> "NO_DRIVER_AVAILABLE";
    };
  }

  record Card(UUID id) {}

  record Payment(UUID id, String status) {}

  record ErrorBody(String message) {}

  record AuthorizationRequest(
      UUID bookingId, UUID passengerId, UUID paymentMethodId, BigDecimal amount, String currency) {}

  record BookingCancelledRequest(
      UUID bookingId, String reason, Instant cancelledAt, Instant scheduledDepartureAt) {}
}
