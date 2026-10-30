package com.fundatechperu.shareway.payments.application;

import com.fundatechperu.shareway.payments.domain.model.CardBrand;
import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import com.fundatechperu.shareway.payments.domain.repository.PaymentMethodRepository;
import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de registro, consulta y desactivación de las tarjetas de un pasajero. */
@Service
public class PaymentMethodService {

  private final PaymentMethodRepository paymentMethods;

  public PaymentMethodService(PaymentMethodRepository paymentMethods) {
    this.paymentMethods = paymentMethods;
  }

  /** Registra una tarjeta tokenizada; falla con 409 si el pasajero ya tiene ese token activo. */
  @Transactional
  public PaymentMethod register(
      UUID passengerId,
      String cardToken,
      CardBrand brand,
      String last4,
      int expMonth,
      int expYear) {
    if (paymentMethods.existsActiveByPassengerIdAndCardToken(passengerId, cardToken)) {
      throw new ConflictException("Card is already registered");
    }
    PaymentMethod method =
        PaymentMethod.register(
            passengerId, cardToken, brand, last4, expMonth, expYear, YearMonth.now());
    return paymentMethods.save(method);
  }

  @Transactional(readOnly = true)
  public List<PaymentMethod> listActive(UUID passengerId) {
    return paymentMethods.findActiveByPassengerId(passengerId);
  }

  /**
   * Desactiva lógicamente una tarjeta propia; una tarjeta ajena o ya inactiva se trata como
   * inexistente.
   */
  @Transactional
  public void deactivate(UUID passengerId, UUID paymentMethodId) {
    PaymentMethod method =
        paymentMethods
            .findById(paymentMethodId)
            .filter(candidate -> candidate.isUsableBy(passengerId))
            .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
    method.deactivate();
    paymentMethods.save(method);
  }
}
