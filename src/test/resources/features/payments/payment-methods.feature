# Ids are UUIDs. A number such as 300 in a step, or {uuid:300} in a body or path, is an
# alias of 00000000-0000-0000-0000-000000000300.
Feature: Passenger payment methods
  As a passenger
  I want to register and manage my tokenized cards
  So that I can pay for my bookings without exposing card data

  Scenario: Passenger registers a valid card
    Given I am authenticated as a PASSENGER with id 1
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_visa_4242", "brand": "VISA", "last4": "4242", "expMonth": 12, "expYear": 2099}
      """
    Then the response status should be 201
    And the response field "brand" should be "VISA"
    And the response field "last4" should be "4242"
    And the response field "cardToken" should not exist

  Scenario: Registering a card with invalid data is rejected
    Given I am authenticated as a PASSENGER with id 1
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_visa_4242", "brand": "VISA", "last4": "42a", "expMonth": 13, "expYear": 2099}
      """
    Then the response status should be 400
    And the response should report an error on field "last4"
    And the response should report an error on field "expMonth"

  Scenario: Registering a card with an unknown brand is rejected
    Given I am authenticated as a PASSENGER with id 1
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_x", "brand": "DINERS", "last4": "4242", "expMonth": 12, "expYear": 2099}
      """
    Then the response status should be 400

  Scenario: Registering an expired card is rejected
    Given I am authenticated as a PASSENGER with id 1
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_old", "brand": "MASTERCARD", "last4": "5555", "expMonth": 1, "expYear": 2020}
      """
    Then the response status should be 400

  Scenario: Registering the same card token twice is a conflict
    Given I am authenticated as a PASSENGER with id 1
    And I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_dup", "brand": "AMEX", "last4": "0005", "expMonth": 6, "expYear": 2099}
      """
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_dup", "brand": "AMEX", "last4": "0005", "expMonth": 6, "expYear": 2099}
      """
    Then the response status should be 409

  Scenario: Registering a card without a token is unauthorized
    Given I am not authenticated
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_visa_4242", "brand": "VISA", "last4": "4242", "expMonth": 12, "expYear": 2099}
      """
    Then the response status should be 401
    And the response field "status" should be "401"

  Scenario: An expired token is unauthorized
    Given I am authenticated with an expired PASSENGER token with id 1
    When I send a GET request to "/api/v1/payment-methods"
    Then the response status should be 401

  Scenario: A token without role claim is unauthorized
    Given I am authenticated with a token without role for user 1
    When I send a GET request to "/api/v1/payment-methods"
    Then the response status should be 401

  Scenario: A driver cannot register payment methods
    Given I am authenticated as a DRIVER with id 2
    When I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_visa_4242", "brand": "VISA", "last4": "4242", "expMonth": 12, "expYear": 2099}
      """
    Then the response status should be 403

  Scenario: Passenger lists only their own active cards
    Given I am authenticated as a PASSENGER with id 1
    And I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_one", "brand": "VISA", "last4": "1111", "expMonth": 12, "expYear": 2099}
      """
    And I save the response field "id" as "firstCardId"
    And I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_two", "brand": "MASTERCARD", "last4": "2222", "expMonth": 12, "expYear": 2099}
      """
    And I am authenticated as a PASSENGER with id 5
    And I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_other", "brand": "VISA", "last4": "3333", "expMonth": 12, "expYear": 2099}
      """
    And I am authenticated as a PASSENGER with id 1
    And I send a DELETE request to "/api/v1/payment-methods/{firstCardId}"
    And the response status should be 204
    When I send a GET request to "/api/v1/payment-methods"
    Then the response status should be 200
    And the response should contain 1 items
    And the response field "[0].last4" should be "2222"

  Scenario: Deleting another passenger's card is not found
    Given I am authenticated as a PASSENGER with id 1
    And I send a POST request to "/api/v1/payment-methods" with body:
      """
      {"cardToken": "tok_mine", "brand": "VISA", "last4": "4242", "expMonth": 12, "expYear": 2099}
      """
    And I save the response field "id" as "cardId"
    And I am authenticated as a PASSENGER with id 9
    When I send a DELETE request to "/api/v1/payment-methods/{cardId}"
    Then the response status should be 404
