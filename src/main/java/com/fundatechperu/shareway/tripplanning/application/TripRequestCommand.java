package com.fundatechperu.shareway.tripplanning.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Data of a new trip request, already validated by the REST layer. */
public record TripRequestCommand(
    UUID passengerId,
    String originLabel,
    String destinationLabel,
    String operationZone,
    BigDecimal originLat,
    BigDecimal originLng,
    BigDecimal destinationLat,
    BigDecimal destinationLng,
    LocalDate travelDate,
    LocalTime earliestDeparture,
    LocalTime latestDeparture,
    int seatsRequested) {}
