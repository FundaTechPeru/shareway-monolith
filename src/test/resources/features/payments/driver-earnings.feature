# Ids are UUIDs. A number such as 300 in a step, or {uuid:300} in a body or path, is an
# alias of 00000000-0000-0000-0000-000000000300.
Feature: Driver earnings and settlement payouts
  As a driver
  I want to see my settlements and earnings
  So that I know how much the platform owes me, and administrators can register the payouts

  Background:
    Given driver 2 has a settlement for trip 1 with gross "70.00" PEN saved as "firstTrip"
    And driver 2 has a settlement for trip 2 with gross "33.33" PEN saved as "secondTrip"
    And driver 7 has a settlement for trip 3 with gross "50.00" PEN saved as "otherDriverTrip"

  Scenario: Driver lists only their own settlements
    Given I am authenticated as a DRIVER with id 2
    When I send a GET request to "/api/v1/settlements/me"
    Then the response status should be 200
    And the response should contain 2 items
    And every item field "driverId" should be "{uuid:2}"

  Scenario: Driver sees the detail of one of their settlements
    Given I am authenticated as a DRIVER with id 2
    When I send a GET request to "/api/v1/settlements/me/{firstTrip}"
    Then the response status should be 200
    And the response field "grossAmount" should be "70.00"
    And the response field "commissionAmount" should be "10.50"
    And the response field "netAmount" should be "59.50"

  Scenario: Driver summary separates pending and paid earnings
    Given I am authenticated as a ADMIN with id 3
    And I send a PATCH request to "/api/v1/admin/settlements/{firstTrip}/mark-paid"
    And I am authenticated as a DRIVER with id 2
    When I send a GET request to "/api/v1/settlements/me/summary"
    Then the response status should be 200
    And the response field "totalGross" should be "103.33"
    And the response field "totalCommission" should be "15.50"
    And the response field "totalNet" should be "87.83"
    And the response field "pendingNet" should be "28.33"
    And the response field "paidNet" should be "59.50"

  Scenario: Driver cannot see another driver's settlement
    Given I am authenticated as a DRIVER with id 2
    When I send a GET request to "/api/v1/settlements/me/{otherDriverTrip}"
    Then the response status should be 404

  Scenario: Passenger cannot read driver settlements
    Given I am authenticated as a PASSENGER with id 1
    When I send a GET request to "/api/v1/settlements/me"
    Then the response status should be 403

  Scenario: Settlements require authentication
    Given I am not authenticated
    When I send a GET request to "/api/v1/settlements/me/summary"
    Then the response status should be 401

  Scenario: Admin filters settlements by status
    Given I am authenticated as a ADMIN with id 3
    And I send a PATCH request to "/api/v1/admin/settlements/{secondTrip}/mark-paid"
    When I send a GET request to "/api/v1/admin/settlements?status=PENDING"
    Then the response status should be 200
    And the response should contain 2 items
    And every item field "status" should be "PENDING"

  Scenario: Admin filter with an unknown status is rejected
    Given I am authenticated as a ADMIN with id 3
    When I send a GET request to "/api/v1/admin/settlements?status=LOST"
    Then the response status should be 400

  Scenario: Admin marks a settlement as paid only once
    Given I am authenticated as a ADMIN with id 3
    When I send a PATCH request to "/api/v1/admin/settlements/{firstTrip}/mark-paid"
    Then the response status should be 200
    And the response field "status" should be "PAID"
    And I send a PATCH request to "/api/v1/admin/settlements/{firstTrip}/mark-paid"
    And the response status should be 409

  Scenario: Marking an unknown settlement is not found
    Given I am authenticated as a ADMIN with id 3
    When I send a PATCH request to "/api/v1/admin/settlements/{uuid:999999}/mark-paid"
    Then the response status should be 404

  Scenario: Driver cannot mark settlements as paid
    Given I am authenticated as a DRIVER with id 2
    When I send a PATCH request to "/api/v1/admin/settlements/{firstTrip}/mark-paid"
    Then the response status should be 403
