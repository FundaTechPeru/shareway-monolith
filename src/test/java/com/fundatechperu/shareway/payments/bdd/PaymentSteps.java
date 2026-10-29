package com.fundatechperu.shareway.payments.bdd;

import com.fundatechperu.shareway.payments.application.PaymentAuthorizationService;
import com.fundatechperu.shareway.payments.domain.model.CardBrand;
import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import com.fundatechperu.shareway.payments.domain.repository.PaymentMethodRepository;
import io.cucumber.java.en.Given;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Steps que preparan datos de pagos directamente sobre los puertos y servicios de la aplicación.
 */
public class PaymentSteps {

  private final PaymentMethodRepository paymentMethods;
  private final PaymentAuthorizationService authorizationService;
  private final ScenarioContext context;

  public PaymentSteps(
      PaymentMethodRepository paymentMethods,
      PaymentAuthorizationService authorizationService,
      ScenarioContext context) {
    this.paymentMethods = paymentMethods;
    this.authorizationService = authorizationService;
    this.context = context;
  }

  @Given("passenger {long} has an active payment method with card token {string} saved as {string}")
  public void passengerHasPaymentMethod(Long passengerId, String cardToken, String name) {
    context.put(name, registerCard(TestIds.uuid(passengerId), cardToken).getId());
  }

  @Given("booking {long} has an authorized payment of {string} PEN for passenger {long}")
  public void bookingHasAuthorizedPayment(Long bookingId, String amount, Long passengerId) {
    PaymentMethod method = registerCard(TestIds.uuid(passengerId), "tok_booking_" + bookingId);
    authorizationService.authorize(
        TestIds.uuid(bookingId),
        TestIds.uuid(passengerId),
        method.getId(),
        new BigDecimal(amount),
        "PEN");
  }

  private PaymentMethod registerCard(UUID passengerId, String cardToken) {
    return paymentMethods.save(
        PaymentMethod.register(
            passengerId, cardToken, CardBrand.VISA, "4242", 12, 2099, YearMonth.now()));
  }
}
