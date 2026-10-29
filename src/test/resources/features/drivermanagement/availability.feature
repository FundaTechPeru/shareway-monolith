Feature: Driver availability
  As a driver
  I want to publish my weekly availability
  So that rides can be scheduled when I am free

  Background:
    Given I am logged in as a driver with email "carlos.diaz@upc.edu.pe"

  Scenario: Set my weekly availability
    When I set my weekly availability to:
      | dayOfWeek | startTime | endTime |
      | WEDNESDAY | 07:30     | 10:00   |
      | MONDAY    | 18:00     | 20:00   |
      | MONDAY    | 07:00     | 09:00   |
    Then the response status should be 200
    And my availability should have 3 slots
    When I request my weekly availability
    Then the response status should be 200
    And my availability should have 3 slots
    And slot 1 should be "MONDAY" from "07:00" to "09:00"
    And slot 2 should be "MONDAY" from "18:00" to "20:00"
    And slot 3 should be "WEDNESDAY" from "07:30" to "10:00"

  Scenario: Replace the whole weekly availability
    Given my weekly availability is:
      | dayOfWeek | startTime | endTime |
      | MONDAY    | 07:00     | 09:00   |
      | TUESDAY   | 07:00     | 09:00   |
    When I set my weekly availability to:
      | dayOfWeek | startTime | endTime |
      | FRIDAY    | 16:00     | 18:00   |
    Then the response status should be 200
    When I request my weekly availability
    Then my availability should have 1 slot
    And slot 1 should be "FRIDAY" from "16:00" to "18:00"

  Scenario: Clear my weekly availability
    Given my weekly availability is:
      | dayOfWeek | startTime | endTime |
      | MONDAY    | 07:00     | 09:00   |
    When I set my weekly availability to an empty list
    Then the response status should be 200
    And my availability should have 0 slots

  Scenario: Accept adjacent slots and the same hours on different days
    When I set my weekly availability to:
      | dayOfWeek | startTime | endTime |
      | MONDAY    | 07:00     | 09:00   |
      | MONDAY    | 09:00     | 11:00   |
      | TUESDAY   | 07:00     | 09:00   |
    Then the response status should be 200
    And my availability should have 3 slots

  Scenario Outline: Reject a slot whose start is not before its end
    When I set my weekly availability to a slot on "MONDAY" from "<startTime>" to "<endTime>"
    Then the request should fail with status 400
    And the response should report errors for fields "slots"

    Examples:
      | startTime | endTime |
      | 10:00     | 09:00   |
      | 10:00     | 10:00   |

  Scenario: Reject overlapping slots on the same day
    When I set my weekly availability to:
      | dayOfWeek | startTime | endTime |
      | MONDAY    | 07:00     | 09:00   |
      | MONDAY    | 08:30     | 10:00   |
    Then the request should fail with status 400
    And the response should report errors for fields "slots"

  Scenario Outline: Reject an invalid day or time
    When I set my weekly availability to a slot on "<dayOfWeek>" from "<startTime>" to "<endTime>"
    Then the request should fail with status 400

    Examples:
      | dayOfWeek | startTime | endTime |
      | FUNDAY    | 07:00     | 09:00   |
      | MONDAY    | 25:00     | 26:00   |
      | MONDAY    | 7am       | 9am     |

  Scenario: Reject a slot without times
    When I set my weekly availability to a slot on "MONDAY" without times
    Then the request should fail with status 400
    And the response should report errors for fields "slots[0].startTime, slots[0].endTime"

  Scenario: Reject a request without slots
    When I send a weekly availability without slots
    Then the request should fail with status 400
    And the response should report errors for fields "slots"

  Scenario: Only drivers can manage their availability
    Given I am logged in as a passenger with email "ana.torres@upc.edu.pe"
    When I request my weekly availability
    Then the request should fail with status 403
