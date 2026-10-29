# Ids are UUIDs. A number such as 300 in a step, or {uuid:300} in a body or path, is an
# alias of 00000000-0000-0000-0000-000000000300.
Feature: Payment capture when a trip is completed (US25)
  As Trip Execution
  I want to notify that a trip was completed
  So that every booking is charged and the driver earnings are settled

  Background:
    Given booking 200 has an authorized payment of "40.00" PEN for passenger 1
    And booking 201 has an authorized payment of "35.50" PEN for passenger 1
    And I use the internal API key

  Scenario: Completed trip captures the bookings and creates a pending settlement
    When I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:900}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z",
       "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 40.00}, {"bookingId": "{uuid:201}", "finalAmount": 30.00}]}
      """
    Then the response status should be 201
    And the response field "grossAmount" should be "70.00"
    And the response field "commissionAmount" should be "10.50"
    And the response field "netAmount" should be "59.50"
    And the response field "status" should be "PENDING"
    And I am authenticated as a PASSENGER with id 1
    And I send a GET request to "/api/v1/payments/me"
    And every item field "status" should be "CAPTURED"

  Scenario: Repeating the same trip completion is idempotent
    Given I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:901}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z",
       "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 33.33}]}
      """
    And I save the response field "id" as "settlementId"
    When I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:901}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z",
       "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 33.33}]}
      """
    Then the response status should be 200
    And the response field "id" should be "{settlementId}"
    And the response field "grossAmount" should be "33.33"
    And the response field "commissionAmount" should be "5.00"
    And the response field "netAmount" should be "28.33"

  Scenario: Final amount above the authorized amount changes nothing
    When I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:902}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z",
       "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 40.00}, {"bookingId": "{uuid:201}", "finalAmount": 35.51}]}
      """
    Then the response status should be 409
    And I am authenticated as a PASSENGER with id 1
    And I send a GET request to "/api/v1/payments/me"
    And every item field "status" should be "AUTHORIZED"

  Scenario: Booking without an authorized payment is a conflict
    When I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:903}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z",
       "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 20.00}, {"bookingId": "{uuid:999}", "finalAmount": 10.00}]}
      """
    Then the response status should be 409
    And I am authenticated as a PASSENGER with id 1
    And I send a GET request to "/api/v1/payments/me"
    And every item field "status" should be "AUTHORIZED"

  Scenario Outline: Invalid trip completed payload is rejected
    When I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      <body>
      """
    Then the response status should be 400

    Examples:
      | body                                                                                                         |
      | {"tripId": "{uuid:904}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z", "bookings": []}                        |
      | {"tripId": "{uuid:904}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z", "bookings": [{"bookingId": "{uuid:200}", "finalAmount": -1}]} |
      | {"tripId": "{uuid:904}", "driverId": "{uuid:2}", "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 10.00}]}                       |

  Scenario: Trip completed without API key is unauthorized
    Given I am not authenticated
    When I send a POST request to "/api/v1/internal/events/trip-completed" with body:
      """
      {"tripId": "{uuid:905}", "driverId": "{uuid:2}", "completedAt": "2026-10-01T15:30:00Z",
       "bookings": [{"bookingId": "{uuid:200}", "finalAmount": 40.00}]}
      """
    Then the response status should be 401
