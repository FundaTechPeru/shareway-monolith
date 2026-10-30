Feature: Driver registration
  As a passenger
  I want to register as a driver
  So that I can offer rides once my documents are verified

  Background:
    Given I am logged in as a passenger with email "carlos.diaz@upc.edu.pe"

  Scenario: Register as a driver
    When I register my driver profile with license number "Q12345678" and emergency contact "Rosa Diaz", "+51911222333", "Mother"
    Then the response status should be 201
    And the response field "licenseNumber" should be "Q12345678"
    And the response field "driverStatus" should be "PENDING_VERIFICATION"
    And the response field "emergencyContact.name" should be "Rosa Diaz"
    And the response field "emergencyContact.relationship" should be "Mother"
    When I request my profile
    Then the response field "role" should be "DRIVER"
    And the response field "driverStatus" should be "PENDING_VERIFICATION"

  Scenario: View my driver profile
    Given I have registered my driver profile with license number "Q12345678"
    When I request my driver profile
    Then the response status should be 200
    And the response field "licenseNumber" should be "Q12345678"
    And the response field "email" should be "carlos.diaz@upc.edu.pe"

  Scenario Outline: Reject an invalid license number
    When I register my driver profile with license number "<licenseNumber>" and emergency contact "Rosa Diaz", "+51911222333", "Mother"
    Then the request should fail with status 400
    And the response should report errors for fields "licenseNumber"

    Examples:
      | licenseNumber |
      | Q123456       |
      | q12345678     |
      | Q123456789012 |
      | Q1234-5678    |
      |               |

  Scenario: Reject a driver profile without an emergency contact
    When I register my driver profile with license number "Q12345678" and no emergency contact
    Then the request should fail with status 400
    And the response should report errors for fields "emergencyContact"

  Scenario: Reject an emergency contact with invalid data
    When I register my driver profile with license number "Q12345678" and emergency contact "", "abc", ""
    Then the request should fail with status 400
    And the response should report errors for fields "emergencyContact.name, emergencyContact.phone, emergencyContact.relationship"

  Scenario: Reject registering twice as a driver
    Given I have registered my driver profile with license number "Q12345678"
    When I register my driver profile with license number "Z87654321" and emergency contact "Rosa Diaz", "+51911222333", "Mother"
    Then the request should fail with status 409

  Scenario: Reject a license number already registered by another driver
    Given a driver is registered with email "maria.lopez@upc.edu.pe" and license number "Q12345678"
    And I am logged in as "carlos.diaz@upc.edu.pe" with password "Secret123"
    When I register my driver profile with license number "Q12345678" and emergency contact "Rosa Diaz", "+51911222333", "Mother"
    Then the request should fail with status 409

  Scenario: Return not found when I am not a driver
    When I request my driver profile
    Then the request should fail with status 404

  Scenario: Reject driver registration without a token
    Given I am not logged in
    When I register my driver profile with license number "Q12345678" and emergency contact "Rosa Diaz", "+51911222333", "Mother"
    Then the request should fail with status 401
