Feature: User registration
  As a university student
  I want to create a ShareWay account
  So that I can use the platform as a passenger

  Scenario: Register a new passenger account
    When I register with the following data:
      | email    | ana.torres@upc.edu.pe |
      | password | Secret123             |
      | fullName | Ana Torres            |
      | phone    | +51987654321          |
    Then the response status should be 201
    And the response field "email" should be "ana.torres@upc.edu.pe"
    And the response field "fullName" should be "Ana Torres"
    And the response field "role" should be "PASSENGER"

  Scenario: Store the email in lower case
    When I register with the following data:
      | email    | Ana.Torres@UPC.edu.pe |
      | password | Secret123             |
      | fullName | Ana Torres            |
      | phone    | +51987654321          |
    Then the response status should be 201
    And the response field "email" should be "ana.torres@upc.edu.pe"

  Scenario: Reject a registration with invalid data
    When I register with the following data:
      | email    | not-an-email |
      | password | short        |
      | fullName |              |
      | phone    | 123          |
    Then the request should fail with status 400
    And the response should report errors for fields "email, password, fullName, phone"

  Scenario Outline: Reject a password without both letters and numbers
    When I register with the following data:
      | email    | ana.torres@upc.edu.pe |
      | password | <password>            |
      | fullName | Ana Torres            |
      | phone    | +51987654321          |
    Then the request should fail with status 400
    And the response should report errors for fields "password"

    Examples:
      | password    |
      | onlyletters |
      | 1234567890  |

  Scenario: Reject a registration with an email already in use
    Given a user is registered with email "ana.torres@upc.edu.pe" and password "Secret123"
    When I register with the following data:
      | email    | ana.torres@upc.edu.pe |
      | password | Another123            |
      | fullName | Ana Maria Torres      |
      | phone    | +51911222333          |
    Then the request should fail with status 409
