package com.fundatechperu.shareway.booking.pricing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FareRule(
    UUID ruleId,
    int version,
    BigDecimal baseAmount,
    BigDecimal amountPerKm,
    BigDecimal platformFeePct,
    LocalDate validFrom,
    LocalDate validTo) {}
