package com.fundatechperu.shareway.tripplanning.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Data of a new dated driver availability, already validated by the REST layer. */
public record DriverAvailabilityCommand(
    UUID driverId,
    String operationZone,
    LocalDate availableDate,
    LocalTime startTime,
    LocalTime endTime,
    int availableSeats) {}
