package com.fundatechperu.shareway.booking.interfaces;

/** Request and response examples of the Booking &amp; Pricing OpenAPI document. */
final class BookingExamples {

  static final String BOOKING_ID = "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f";
  static final String GROUP_ID = "11111111-1111-1111-1111-111111111111";
  static final String RULE_ID = "0b6a7f3e-1c2d-4e5f-8a9b-0c1d2e3f4a5b";

  static final String CREATE_BOOKING_REQUEST =
      """
      {
        "groupId": "11111111-1111-1111-1111-111111111111",
        "requestId": "22222222-2222-2222-2222-222222222222"
      }
      """;

  static final String BOOKING_RESPONSE =
      """
      {"bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f", "status": "REQUESTED"}
      """;

  static final String NO_SEATS =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "El viaje ya no tiene asientos disponibles.",
        "path": "/api/v1/bookings",
        "fieldErrors": []
      }
      """;

  static final String CANCELLATION_RESPONSE =
      """
      {
        "bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "status": "CANCELLED",
        "penaltyAmount": 4.27,
        "currency": "PEN"
      }
      """;

  static final String CANNOT_CANCEL =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "La reserva ya no puede cancelarse en su estado actual: BOARDED",
        "path": "/api/v1/bookings/3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f/cancel",
        "fieldErrors": []
      }
      """;

  static final String BOOKING_NOT_FOUND =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 404,
        "error": "Not Found",
        "message": "Booking not found: 3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "path": "/api/v1/bookings/3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "fieldErrors": []
      }
      """;

  static final String GROUP_NOT_FOUND =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 404,
        "error": "Not Found",
        "message": "Group not found: 11111111-1111-1111-1111-111111111111",
        "path": "/api/v1/fares/estimate",
        "fieldErrors": []
      }
      """;

  static final String FARE_ESTIMATE_RESPONSE =
      """
      {"amount": 21.34, "currency": "PEN", "ruleVersion": 1}
      """;

  static final String FARE_RULE_RESPONSE =
      """
      {
        "ruleId": "0b6a7f3e-1c2d-4e5f-8a9b-0c1d2e3f4a5b",
        "version": 1,
        "baseAmount": 5.00,
        "amountPerKm": 1.20,
        "platformFeePct": 10.00,
        "validFrom": "2020-01-01",
        "validTo": "2099-12-31"
      }
      """;

  static final String FARE_RULE_LIST_RESPONSE = "[" + FARE_RULE_RESPONSE + "]";

  private BookingExamples() {}
}
