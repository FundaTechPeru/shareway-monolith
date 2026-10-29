package com.fundatechperu.shareway.payments.application;

import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.domain.repository.PaymentRepository;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas de solo lectura sobre los pagos de un pasajero. */
@Service
@Transactional(readOnly = true)
public class PaymentQueryService {

  private final PaymentRepository payments;

  public PaymentQueryService(PaymentRepository payments) {
    this.payments = payments;
  }

  public List<Payment> listPassengerPayments(UUID passengerId) {
    return payments.findByPassengerId(passengerId);
  }

  /** Devuelve un pago del pasajero; uno ajeno se trata como inexistente. */
  public Payment getPassengerPayment(UUID passengerId, UUID paymentId) {
    return payments
        .findByIdAndPassengerId(paymentId, passengerId)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
  }
}
