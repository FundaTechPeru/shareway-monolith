package com.fundatechperu.shareway.payments.interfaces.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

/** Solicitud de autorización enviada por Booking &amp; Pricing con el monto ya calculado. */
public record AuthorizePaymentRequest(
    @NotNull UUID bookingId,
    @NotNull UUID passengerId,
    @NotNull UUID paymentMethodId,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 10, fraction = 2)
        BigDecimal amount,
    @NotBlank @Pattern(regexp = "PEN", message = "only PEN is supported") String currency) {}
