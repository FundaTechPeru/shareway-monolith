package com.fundatechperu.shareway.tripplanning.interfaces.rest.dto;

import com.fundatechperu.shareway.tripplanning.domain.model.TripRequest;
import com.fundatechperu.shareway.tripplanning.domain.model.TripRequestStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TripRequestResponse(
    UUID id,
    UUID passengerId,
    String originLabel,
    String destinationLabel,
    String operationZone,
    LocalDate travelDate,
    LocalTime earliestDeparture,
    LocalTime latestDeparture,
    int seatsRequested,
    TripRequestStatus status,
    Instant createdAt) {

  public static TripRequestResponse from(TripRequest tripRequest) {
    return new TripRequestResponse(
        tripRequest.getId(),
        tripRequest.getPassengerId(),
        tripRequest.getOriginLabel(),
        tripRequest.getDestinationLabel(),
        tripRequest.getOperationZone(),
        tripRequest.getTravelDate(),
        tripRequest.getEarliestDeparture(),
        tripRequest.getLatestDeparture(),
        tripRequest.getSeatsRequested(),
        tripRequest.getStatus(),
        tripRequest.getCreatedAt());
  }
}
