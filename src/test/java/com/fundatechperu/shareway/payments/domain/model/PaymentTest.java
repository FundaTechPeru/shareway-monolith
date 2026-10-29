package com.fundatechperu.shareway.payments.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias de las transiciones de estado del agregado {@link Payment}. */
class PaymentTest {

  private static final Money AMOUNT = Money.of(new BigDecimal("30.00"), "PEN");

  @Test
  void authorizesPendingPayment() {
    Payment payment = Payment.create(new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), AMOUNT);

    payment.markAuthorized("sim_ref", Instant.now());

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(payment.isAuthorizedOrCaptured()).isTrue();
  }

  @Test
  void rejectsNonPositiveAmount() {
    assertThatThrownBy(
            () ->
                Payment.create(
                    new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), Money.zero("PEN")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void cannotAuthorizeTwice() {
    Payment payment = Payment.create(new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), AMOUNT);
    payment.markAuthorized("sim_ref", Instant.now());

    assertThatThrownBy(() -> payment.markAuthorized("sim_other", Instant.now()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void rejectedPaymentCanBeRetriedBySamePassenger() {
    Payment payment = Payment.create(new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), AMOUNT);
    payment.markRejected();

    payment.retry(new UUID(0, 1), new UUID(0, 11), AMOUNT);
    payment.markAuthorized("sim_ref", Instant.now());

    assertThat(payment.getPaymentMethodId()).isEqualTo(new UUID(0, 11));
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
  }

  @Test
  void capturesUpToTheAuthorizedAmount() {
    Payment payment = authorizedPayment();

    payment.capture(Money.of(new BigDecimal("28.40"), "PEN"), Instant.now());

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    assertThat(payment.getCapturedAmount().getAmount()).isEqualTo(new BigDecimal("28.40"));
  }

  @Test
  void cannotCaptureMoreThanAuthorized() {
    Payment payment = authorizedPayment();

    assertThatThrownBy(
            () -> payment.capture(Money.of(new BigDecimal("30.01"), "PEN"), Instant.now()))
        .isInstanceOf(ConflictException.class);
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
  }

  @Test
  void cannotCaptureRejectedPayment() {
    Payment payment = Payment.create(new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), AMOUNT);
    payment.markRejected();

    assertThatThrownBy(() -> payment.capture(AMOUNT, Instant.now()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void cannotCaptureTwice() {
    Payment payment = authorizedPayment();
    payment.capture(AMOUNT, Instant.now());

    assertThatThrownBy(() -> payment.capture(AMOUNT, Instant.now()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void cannotCaptureVoidedPayment() {
    Payment payment = authorizedPayment();
    payment.voidAuthorization(CancellationReason.NO_DRIVER_AVAILABLE, Instant.now());

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VOIDED);
    assertThatThrownBy(() -> payment.capture(AMOUNT, Instant.now()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void cannotVoidCapturedPayment() {
    Payment payment = authorizedPayment();
    payment.capture(AMOUNT, Instant.now());

    assertThatThrownBy(
            () -> payment.voidAuthorization(CancellationReason.PASSENGER_CANCELLED, Instant.now()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void penaltyCapturesOnlyThePenaltyAmount() {
    Payment payment = authorizedPayment();

    payment.chargePenalty(
        Money.of(new BigDecimal("15.00"), "PEN"), CancellationReason.NO_SHOW, Instant.now());

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    assertThat(payment.getCapturedAmount().getAmount()).isEqualTo(new BigDecimal("15.00"));
    assertThat(payment.isCancellationResolved()).isTrue();
  }

  private Payment authorizedPayment() {
    Payment payment = Payment.create(new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), AMOUNT);
    payment.markAuthorized("sim_ref", Instant.now());
    return payment;
  }

  @Test
  void authorizedPaymentCannotBeRetried() {
    Payment payment = Payment.create(new UUID(0, 100), new UUID(0, 1), new UUID(0, 10), AMOUNT);
    payment.markAuthorized("sim_ref", Instant.now());

    assertThatThrownBy(() -> payment.retry(new UUID(0, 1), new UUID(0, 11), AMOUNT))
        .isInstanceOf(ConflictException.class);
  }
}
