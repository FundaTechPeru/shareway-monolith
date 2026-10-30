Feature: Vehicle registration
  As a driver
  I want to register my vehicles
  So that passengers know which car will pick them up

  Background:
    Given I am logged in as a driver with email "carlos.diaz@upc.edu.pe"

  Scenario: Register a vehicle before being verified
    When I register a vehicle with plate "abc-123", brand "Toyota", model "Yaris", year 2020, color "Red" and 4 seats
    Then the response status should be 201
    And the response field "plate" should be "ABC-123"
    And the response field "brand" should be "Toyota"
    And the response field "year" should be "2020"
    And the response field "seats" should be "4"
    And the driver "carlos.diaz@upc.edu.pe" should have status "PENDING_VERIFICATION"

  Scenario: Normalize a plate written without a hyphen
    When I register a vehicle with plate "xyz789", brand "Kia", model "Rio", year 2019, color "White" and 4 seats
    Then the response status should be 201
    And the response field "plate" should be "XYZ-789"

  Scenario: Accept next year's model
    When I register a vehicle with plate "NXT-001" for next year's model
    Then the response status should be 201

  Scenario: List, view and update my vehicles
    Given I have registered a vehicle with plate "ABC-123"
    And I have registered a vehicle with plate "DEF-456"
    When I list my vehicles
    Then the response status should be 200
    And the response should contain 2 items
    When I view my vehicle with plate "ABC-123"
    Then the response status should be 200
    And the response field "plate" should be "ABC-123"
    When I update my vehicle with plate "ABC-123" to color "Blue" and 5 seats
    Then the response status should be 200
    And the response field "color" should be "Blue"
    And the response field "seats" should be "5"

  Scenario Outline: Reject invalid vehicle data
    When I register a vehicle with plate "<plate>", brand "Toyota", model "Yaris", year <year>, color "Red" and <seats> seats
    Then the request should fail with status 400
    And the response should report errors for fields "<field>"

    Examples:
      | plate    | year | seats | field |
      | AB12     | 2020 | 4     | plate |
      | ABCD-123 | 2020 | 4     | plate |
      | AB_123   | 2020 | 4     | plate |
      | ABC-123  | 1999 | 4     | year  |
      | ABC-123  | 2100 | 4     | year  |
      | ABC-123  | 2020 | 0     | seats |
      | ABC-123  | 2020 | 9     | seats |

  Scenario: Reject a vehicle without data
    When I register a vehicle with no data
    Then the request should fail with status 400
    And the response should report errors for fields "plate, brand, model, year, color, seats"

  Scenario: Reject a plate that is already registered
    Given a driver "maria.lopez@upc.edu.pe" has registered a vehicle with plate "ABC-123"
    And I am logged in as "carlos.diaz@upc.edu.pe" with password "Secret123"
    When I register a vehicle with plate "abc123", brand "Toyota", model "Yaris", year 2020, color "Red" and 4 seats
    Then the request should fail with status 409

  Scenario: Reject changing a plate to one used by another vehicle
    Given I have registered a vehicle with plate "ABC-123"
    And I have registered a vehicle with plate "DEF-456"
    When I update my vehicle with plate "DEF-456" to plate "ABC-123"
    Then the request should fail with status 409

  Scenario: Hide vehicles that belong to another driver
    Given a driver "maria.lopez@upc.edu.pe" has registered a vehicle with plate "MAR-001"
    And I am logged in as "carlos.diaz@upc.edu.pe" with password "Secret123"
    When I view the vehicle with plate "MAR-001"
    Then the request should fail with status 404
    When I update the vehicle with plate "MAR-001" to color "Black" and 4 seats
    Then the request should fail with status 404

  Scenario: Reject vehicle registration from passengers
    Given I am logged in as a passenger with email "ana.torres@upc.edu.pe"
    When I register a vehicle with plate "ABC-123", brand "Toyota", model "Yaris", year 2020, color "Red" and 4 seats
    Then the request should fail with status 403
