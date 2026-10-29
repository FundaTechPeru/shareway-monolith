# Ids are UUIDs. A number such as 300 in a step, or {uuid:300} in a body or path, is an
# alias of 00000000-0000-0000-0000-000000000300.
Feature: Cancellation penalty (US12)
  As the Booking & Pricing service
  I want to notify booking cancellations
  So that late cancellations and no-shows are penalized and other cancellations are released

  Background:
    Given booking 300 has an authorized payment of "40.00" PEN for passenger 1
    And I use the internal API key

  Scenario: Late passenger cancellation charges the penalty
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "PASSENGER_CANCELLED",
       "cancelledAt": "2026-10-10T07:50:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 200
    And the response field "penaltyApplied" should be "true"
    And the response field "penaltyAmount" should be "20.00"
    And the response field "paymentStatus" should be "CAPTURED"

  Scenario: Early passenger cancellation voids the authorization
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "PASSENGER_CANCELLED",
       "cancelledAt": "2026-10-10T05:00:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 200
    And the response field "penaltyApplied" should be "false"
    And the response field "paymentStatus" should be "VOIDED"

  Scenario: No-show is always penalized
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "NO_SHOW",
       "cancelledAt": "2026-10-10T08:05:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 200
    And the response field "penaltyApplied" should be "true"
    And the response field "penaltyAmount" should be "20.00"

  Scenario: No driver available never penalizes the passenger
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "NO_DRIVER_AVAILABLE",
       "cancelledAt": "2026-10-10T07:59:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 200
    And the response field "penaltyApplied" should be "false"
    And the response field "paymentStatus" should be "VOIDED"

  Scenario: Repeating the same cancellation returns the same result
    Given I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "PASSENGER_CANCELLED",
       "cancelledAt": "2026-10-10T07:50:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    And I save the response field "penaltyId" as "penaltyId"
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "PASSENGER_CANCELLED",
       "cancelledAt": "2026-10-10T07:50:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 200
    And the response field "penaltyId" should be "{penaltyId}"
    And the response field "penaltyAmount" should be "20.00"

  Scenario: Cancelling a booking without payment is not found
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:999}", "reason": "NO_SHOW",
       "cancelledAt": "2026-10-10T08:05:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 404

  Scenario: Cancelling an already captured booking is a conflict
    Given I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:950}", "driverId": "{uuid:2}", "completedAt": "2026-10-10T09:00:00Z",
       "bookings": [{"bookingId": "{uuid:300}", "finalAmount": 40.00}]}
      """
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "PASSENGER_CANCELLED",
       "cancelledAt": "2026-10-10T05:00:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 409

  Scenario Outline: Invalid cancellation payload is rejected
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      <body>
      """
    Then the response status should be 400

    Examples:
      | body                                                                                                                  |
      | {"bookingId": "{uuid:300}", "reason": "CHANGED_MIND", "cancelledAt": "2026-10-10T07:50:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"} |
      | {"bookingId": "{uuid:300}", "reason": "NO_SHOW", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}                               |
      | {"bookingId": "not-a-uuid", "reason": "NO_SHOW", "cancelledAt": "2026-10-10T07:50:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"} |

  Scenario: Booking cancelled without API key is unauthorized
    Given I am not authenticated
    When I send a POST request to "/api/v1/internal/events/booking-cancelled" with body:
      """
      {"bookingId": "{uuid:300}", "reason": "NO_SHOW",
       "cancelledAt": "2026-10-10T08:05:00Z", "scheduledDepartureAt": "2026-10-10T08:00:00Z"}
      """
    Then the response status should be 401
