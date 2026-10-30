package com.fundatechperu.shareway.payments.interfaces;

/** Request and response examples of the Payments &amp; Settlement OpenAPI document. */
public final class PaymentsExamples {

  public static final String PAYMENT_METHOD_ID = "5a7c9e1b-3d5f-4a6b-8c9d-0e1f2a3b4c5d";
  public static final String PAYMENT_ID = "9b8a7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d";
  public static final String SETTLEMENT_ID = "2f4e6d8c-0b1a-4c3d-8e5f-7a9b1c3d5e7f";

  public static final String REGISTER_CARD_REQUEST =
      """
      {
        "cardToken": "tok_visa_4242",
        "brand": "VISA",
        "last4": "4242",
        "expMonth": 12,
        "expYear": 2029
      }
      """;

  public static final String CARD_RESPONSE =
      """
      {
        "id": "5a7c9e1b-3d5f-4a6b-8c9d-0e1f2a3b4c5d",
        "brand": "VISA",
        "last4": "4242",
        "expMonth": 12,
        "expYear": 2029
      }
      """;

  public static final String CARD_LIST_RESPONSE = "[" + CARD_RESPONSE + "]";

  public static final String CARD_TAKEN =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "Card is already registered",
        "path": "/api/v1/payment-methods",
        "fieldErrors": []
      }
      """;

  public static final String PAYMENT_RESPONSE =
      """
      {
        "id": "9b8a7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d",
        "bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "passengerId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "paymentMethodId": "5a7c9e1b-3d5f-4a6b-8c9d-0e1f2a3b4c5d",
        "status": "AUTHORIZED",
        "authorizedAmount": 25.50,
        "capturedAmount": null,
        "currency": "PEN",
        "providerRef": "sim_1c2d3e4f-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
        "authorizedAt": "2026-10-29T14:05:00Z",
        "capturedAt": null,
        "voidedAt": null
      }
      """;

  public static final String PAYMENT_LIST_RESPONSE = "[" + PAYMENT_RESPONSE + "]";

  public static final String AUTHORIZE_REQUEST =
      """
      {
        "bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "passengerId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "paymentMethodId": "5a7c9e1b-3d5f-4a6b-8c9d-0e1f2a3b4c5d",
        "amount": 25.50,
        "currency": "PEN"
      }
      """;

  public static final String PAYMENT_DECLINED =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 402,
        "error": "Payment Required",
        "message": "Payment was declined by the gateway",
        "path": "/api/v1/internal/payments/authorizations",
        "fieldErrors": []
      }
      """;

  public static final String GATEWAY_UNAVAILABLE =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 503,
        "error": "Service Unavailable",
        "message": "Payment gateway is unavailable",
        "path": "/api/v1/internal/payments/authorizations",
        "fieldErrors": []
      }
      """;

  public static final String INVALID_API_KEY =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 401,
        "error": "Unauthorized",
        "message": "Missing or invalid internal API key",
        "path": "/api/v1/internal/payments/authorizations",
        "fieldErrors": []
      }
      """;

  public static final String SETTLEMENT_RESPONSE =
      """
      {
        "id": "2f4e6d8c-0b1a-4c3d-8e5f-7a9b1c3d5e7f",
        "tripId": "8e7d6c5b-4a39-4281-9f0e-1d2c3b4a5968",
        "driverId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "grossAmount": 70.00,
        "commissionAmount": 10.50,
        "netAmount": 59.50,
        "currency": "PEN",
        "status": "PENDING",
        "tripCompletedAt": "2026-10-29T15:30:00Z",
        "createdAt": "2026-10-29T15:30:02Z",
        "paidAt": null
      }
      """;

  public static final String SETTLEMENT_LIST_RESPONSE = "[" + SETTLEMENT_RESPONSE + "]";

  public static final String PAID_SETTLEMENT_RESPONSE =
      """
      {
        "id": "2f4e6d8c-0b1a-4c3d-8e5f-7a9b1c3d5e7f",
        "tripId": "8e7d6c5b-4a39-4281-9f0e-1d2c3b4a5968",
        "driverId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "grossAmount": 70.00,
        "commissionAmount": 10.50,
        "netAmount": 59.50,
        "currency": "PEN",
        "status": "PAID",
        "tripCompletedAt": "2026-10-29T15:30:00Z",
        "createdAt": "2026-10-29T15:30:02Z",
        "paidAt": "2026-10-30T09:00:00Z"
      }
      """;

  public static final String SETTLEMENT_ALREADY_PAID =
      """
      {
        "timestamp": "2026-10-30T09:01:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "Settlement is already paid",
        "path": "/api/v1/admin/settlements/2f4e6d8c-0b1a-4c3d-8e5f-7a9b1c3d5e7f/mark-paid",
        "fieldErrors": []
      }
      """;

  public static final String EARNINGS_SUMMARY_RESPONSE =
      """
      {
        "totalGross": 103.33,
        "totalCommission": 15.50,
        "totalNet": 87.83,
        "pendingNet": 28.33,
        "paidNet": 59.50
      }
      """;

  public static final String TRIP_COMPLETED_REQUEST =
      """
      {
        "tripId": "8e7d6c5b-4a39-4281-9f0e-1d2c3b4a5968",
        "driverId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "completedAt": "2026-10-29T15:30:00Z",
        "bookings": [
          {"bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f", "finalAmount": 40.00},
          {"bookingId": "6c5d4e3f-2a1b-4c0d-9e8f-7a6b5c4d3e2f", "finalAmount": 30.00}
        ]
      }
      """;

  public static final String CAPTURE_CONFLICT =
      """
      {
        "timestamp": "2026-10-29T15:30:01Z",
        "status": 409,
        "error": "Conflict",
        "message": "Captured amount exceeds the authorized amount for booking \
      3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "path": "/api/v1/internal/events/trip-completed",
        "fieldErrors": []
      }
      """;

  public static final String BOOKING_CANCELLED_REQUEST =
      """
      {
        "bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "reason": "PASSENGER_CANCELLED",
        "cancelledAt": "2026-10-29T07:50:00Z",
        "scheduledDepartureAt": "2026-10-29T08:00:00Z"
      }
      """;

  public static final String CANCELLATION_RESPONSE =
      """
      {
        "bookingId": "3f0f6a3c-2a3b-4c6d-9d1e-0a1b2c3d4e5f",
        "paymentId": "9b8a7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d",
        "paymentStatus": "CAPTURED",
        "reason": "PASSENGER_CANCELLED",
        "penaltyApplied": true,
        "penaltyId": "4d3c2b1a-0f9e-4d8c-b7a6-5f4e3d2c1b0a",
        "penaltyAmount": 20.00,
        "currency": "PEN"
      }
      """;

  public static final String PAYMENT_NOT_AUTHORIZED =
      """
      {
        "timestamp": "2026-10-29T07:50:01Z",
        "status": 409,
        "error": "Conflict",
        "message": "Payment must be AUTHORIZED but is CAPTURED",
        "path": "/api/v1/internal/events/booking-cancelled",
        "fieldErrors": []
      }
      """;

  private PaymentsExamples() {}
}
