# Ids are UUIDs. A number such as 300 in a step, or {uuid:300} in a body or path, is an
# alias of 00000000-0000-0000-0000-000000000300.
Feature: Payment authorization when booking (US04)
  As the Booking & Pricing saga
  I want to authorize the passenger's payment when a seat is booked
  So that the trip can be charged once it is completed

  Background:
    Given passenger 1 has an active payment method with card token "tok_visa_ok" saved as "cardId"
    And passenger 1 has an active payment method with card token "tok_declined" saved as "declinedCardId"
    And passenger 1 has an active payment method with card token "tok_unavailable" saved as "downCardId"
    And I use the internal API key

  Scenario: Approved authorization creates an AUTHORIZED payment
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:100}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 25.50, "currency": "PEN"}
      """
    Then the response status should be 201
    And the response field "status" should be "AUTHORIZED"
    And the response field "authorizedAmount" should be "25.50"
    And the response field "providerRef" should start with "sim_"

  Scenario: Repeating the same authorization is idempotent
    Given I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:101}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 18.00, "currency": "PEN"}
      """
    And I save the response field "id" as "paymentId"
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:101}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 18.00, "currency": "PEN"}
      """
    Then the response status should be 200
    And the response field "id" should be "{paymentId}"
    And the response field "status" should be "AUTHORIZED"

  Scenario: Declined card returns 402 and stores a REJECTED payment that can be retried
    Given I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:102}", "passengerId": "{uuid:1}", "paymentMethodId": "{declinedCardId}", "amount": 30.00, "currency": "PEN"}
      """
    And the response status should be 402
    And I am authenticated as a PASSENGER with id 1
    And I send a GET request to "/api/v1/payments/me"
    And the response field "[0].status" should be "REJECTED"
    And I save the response field "[0].id" as "rejectedId"
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:102}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 30.00, "currency": "PEN"}
      """
    Then the response status should be 201
    And the response field "id" should be "{rejectedId}"
    And the response field "status" should be "AUTHORIZED"

  Scenario: Gateway outage returns 503 and stores no authorized payment
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:103}", "passengerId": "{uuid:1}", "paymentMethodId": "{downCardId}", "amount": 30.00, "currency": "PEN"}
      """
    Then the response status should be 503
    And I am authenticated as a PASSENGER with id 1
    And I send a GET request to "/api/v1/payments/me"
    And the response should contain 0 items

  Scenario Outline: Invalid amount or currency is rejected
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:104}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": <amount>, "currency": "<currency>"}
      """
    Then the response status should be 400

    Examples:
      | amount | currency |
      | 0      | PEN      |
      | -5.00  | PEN      |
      | 10.123 | PEN      |
      | 10.00  | USD      |

  Scenario: Payment method of another passenger is not found
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:105}", "passengerId": "{uuid:2}", "paymentMethodId": "{cardId}", "amount": 10.00, "currency": "PEN"}
      """
    Then the response status should be 404

  Scenario: Internal endpoint without API key is unauthorized
    Given I am not authenticated
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:106}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 10.00, "currency": "PEN"}
      """
    Then the response status should be 401

  Scenario: Internal endpoint with a wrong API key is unauthorized
    Given I use the internal API key "wrong-key"
    When I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:106}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 10.00, "currency": "PEN"}
      """
    Then the response status should be 401

  Scenario: Passenger cannot see another passenger's payment
    Given I send a POST request to "/api/v1/internal/payments/authorizations" with body:
      """
      {"bookingId": "{uuid:107}", "passengerId": "{uuid:1}", "paymentMethodId": "{cardId}", "amount": 10.00, "currency": "PEN"}
      """
    And I save the response field "id" as "paymentId"
    And I am authenticated as a PASSENGER with id 2
    When I send a GET request to "/api/v1/payments/me/{paymentId}"
    Then the response status should be 404

  Scenario: Driver cannot read passenger payments
    Given I am authenticated as a DRIVER with id 2
    When I send a GET request to "/api/v1/payments/me"
    Then the response status should be 403
