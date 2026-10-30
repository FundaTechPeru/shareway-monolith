package com.fundatechperu.shareway.tripplanning.interfaces.rest;

/** Request and response examples of the Trip Planning &amp; Matching OpenAPI document. */
final class TripPlanningExamples {

  static final String TRIP_REQUEST_ID = "5e6f7a8b-9c0d-4e1f-a2b3-c4d5e6f7a8b9";

  static final String CREATE_TRIP_REQUEST =
      """
      {
        "passengerId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "originLabel": "UPC Monterrico",
        "destinationLabel": "Jockey Plaza",
        "operationZone": "Surco",
        "originLat": -12.104,
        "originLng": -76.963,
        "destinationLat": -12.085,
        "destinationLng": -76.976,
        "travelDate": "2026-11-03",
        "earliestDeparture": "07:00",
        "latestDeparture": "07:30",
        "seatsRequested": 2
      }
      """;

  static final String TRIP_REQUEST_RESPONSE =
      """
      {
        "id": "5e6f7a8b-9c0d-4e1f-a2b3-c4d5e6f7a8b9",
        "passengerId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "originLabel": "UPC Monterrico",
        "destinationLabel": "Jockey Plaza",
        "operationZone": "Surco",
        "travelDate": "2026-11-03",
        "earliestDeparture": "07:00:00",
        "latestDeparture": "07:30:00",
        "seatsRequested": 2,
        "status": "PENDING_MATCH",
        "createdAt": "2026-10-29T14:05:00Z"
      }
      """;

  static final String TRIP_REQUEST_LIST_RESPONSE = "[" + TRIP_REQUEST_RESPONSE + "]";

  static final String CREATE_AVAILABILITY_REQUEST =
      """
      {
        "driverId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "operationZone": "Surco",
        "availableDate": "2026-11-03",
        "startTime": "06:30",
        "endTime": "08:00",
        "availableSeats": 3
      }
      """;

  static final String AVAILABILITY_RESPONSE =
      """
      {
        "id": "8f9a0b1c-2d3e-4f5a-b6c7-d8e9f0a1b2c3",
        "driverId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "operationZone": "Surco",
        "availableDate": "2026-11-03",
        "startTime": "06:30:00",
        "endTime": "08:00:00",
        "availableSeats": 3
      }
      """;

  static final String PROPOSAL_LIST_RESPONSE =
      """
      [
        {
          "id": "c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f",
          "driverId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
          "estimatedFare": 10.90,
          "estimatedDurationMinutes": 30,
          "status": "PROPOSED",
          "createdAt": "2026-10-29T14:06:00Z"
        }
      ]
      """;

  static final String TRIP_REQUEST_NOT_FOUND =
      """
      {
        "timestamp": "2026-10-29T14:06:00Z",
        "status": 404,
        "error": "Not Found",
        "message": "Trip request not found: 5e6f7a8b-9c0d-4e1f-a2b3-c4d5e6f7a8b9",
        "path": "/api/v1/trip-requests/5e6f7a8b-9c0d-4e1f-a2b3-c4d5e6f7a8b9/match-proposals",
        "fieldErrors": []
      }
      """;

  static final String SERVICE_INFO_RESPONSE =
      """
      {
        "service": "ShareWay Trip Planning & Matching",
        "status": "UP",
        "description": "Trip requests, driver availability and matching proposals.",
        "health": "/actuator/health",
        "swagger": "/swagger-ui.html",
        "openapi": "/v3/api-docs/tripplanning",
        "endpoints": ["GET /api/v1/trip-requests", "POST /api/v1/trip-requests"]
      }
      """;

  private TripPlanningExamples() {}
}
