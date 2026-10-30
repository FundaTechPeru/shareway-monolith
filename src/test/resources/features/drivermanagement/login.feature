Feature: Login
  As a registered user
  I want to log in with my credentials
  So that I can use the protected features with an access token

  Background:
    Given a user is registered with email "luis.rojas@upc.edu.pe" and password "Secret123"

  Scenario: Log in with valid credentials
    When I log in with email "luis.rojas@upc.edu.pe" and password "Secret123"
    Then I should receive a bearer access token

  Scenario: Reject a login request with missing data
    When I log in with email "" and password ""
    Then the request should fail with status 400
    And the response should report errors for fields "email, password"

  Scenario: Reject a login with a wrong password
    When I log in with email "luis.rojas@upc.edu.pe" and password "Wrong1234"
    Then the request should fail with status 401

  Scenario: Reject a login for an unknown email
    When I log in with email "nobody@upc.edu.pe" and password "Secret123"
    Then the request should fail with status 401

  Scenario: Access my profile with the issued token
    Given I am logged in as "luis.rojas@upc.edu.pe" with password "Secret123"
    When I request my profile
    Then the response status should be 200
    And the response field "email" should be "luis.rojas@upc.edu.pe"
    And the response field "role" should be "PASSENGER"

  Scenario: Reject profile access without a token
    When I request my profile without a token
    Then the request should fail with status 401

  Scenario: Reject profile access with an invalid token
    When I request my profile with the token "not-a-valid-jwt"
    Then the request should fail with status 401
