Feature: Driver document upload and review
  As a driver
  I want to upload my verification documents
  So that an administrator can verify me and I can offer rides

  Background:
    Given I am logged in as a driver with email "carlos.diaz@upc.edu.pe"

  Scenario: Upload a driver's license in PDF
    When I upload a "DRIVERS_LICENSE" document named "license.pdf" of type "application/pdf"
    Then the response status should be 201
    And the response field "type" should be "DRIVERS_LICENSE"
    And the response field "status" should be "PENDING"
    And the response field "originalFilename" should be "license.pdf"
    And the response field "contentType" should be "application/pdf"
    And the uploaded file should be stored

  Scenario Outline: Accept PDF, JPEG and PNG files
    When I upload a "NATIONAL_ID" document named "<filename>" of type "<contentType>"
    Then the response status should be 201
    And the response field "contentType" should be "<contentType>"

    Examples:
      | filename | contentType     |
      | id.pdf   | application/pdf |
      | id.jpg   | image/jpeg      |
      | id.png   | image/png       |

  Scenario: Reject an unknown document type
    When I upload a "PASSPORT" document named "passport.pdf" of type "application/pdf"
    Then the request should fail with status 400
    And the response should report errors for fields "type"

  Scenario: Reject an empty file
    When I upload an empty "DRIVERS_LICENSE" document
    Then the request should fail with status 400
    And the response should report errors for fields "file"

  Scenario: Reject a file larger than 5 MB
    When I upload a "DRIVERS_LICENSE" document larger than 5 MB
    Then the request should fail with status 400
    And the response should report errors for fields "file"

  Scenario: Reject a file type that is not allowed
    When I upload a "DRIVERS_LICENSE" document named "notes.txt" of type "text/plain"
    Then the request should fail with status 400
    And the response should report errors for fields "file"

  Scenario: Reject a file whose content does not match its declared type
    When I upload a "DRIVERS_LICENSE" document named "fake.pdf" of type "application/pdf" with text content
    Then the request should fail with status 400
    And the response should report errors for fields "file"

  Scenario: Reject uploads from users who are not drivers
    Given I am logged in as a passenger with email "ana.torres@upc.edu.pe"
    When I upload a "DRIVERS_LICENSE" document named "license.pdf" of type "application/pdf"
    Then the request should fail with status 403

  Scenario: List and view my documents
    Given I have uploaded a "DRIVERS_LICENSE" document
    And I have uploaded a "NATIONAL_ID" document
    When I list my documents
    Then the response status should be 200
    And the response should contain 2 items
    When I view my "NATIONAL_ID" document
    Then the response status should be 200
    And the response field "type" should be "NATIONAL_ID"

  Scenario: Hide documents that belong to another driver
    Given a driver "maria.lopez@upc.edu.pe" has uploaded a "DRIVERS_LICENSE" document
    And I am logged in as "carlos.diaz@upc.edu.pe" with password "Secret123"
    When I view the "DRIVERS_LICENSE" document uploaded by "maria.lopez@upc.edu.pe"
    Then the request should fail with status 404

  Scenario: Verify the driver when the three required documents are approved
    Given I have uploaded a "DRIVERS_LICENSE" document
    And I have uploaded a "NATIONAL_ID" document
    And I have uploaded a "CRIMINAL_RECORD" document
    And I am logged in as an administrator
    When I approve the "DRIVERS_LICENSE" document of "carlos.diaz@upc.edu.pe"
    And I approve the "NATIONAL_ID" document of "carlos.diaz@upc.edu.pe"
    Then the driver "carlos.diaz@upc.edu.pe" should have status "PENDING_VERIFICATION"
    And no "DriverVerified" event should have been published
    When I approve the "CRIMINAL_RECORD" document of "carlos.diaz@upc.edu.pe"
    Then the response status should be 200
    And the response field "status" should be "APPROVED"
    And the driver "carlos.diaz@upc.edu.pe" should have status "VERIFIED"
    And a "DriverVerified" event should have been published

  Scenario: Reject a document with a reason
    Given I have uploaded a "CRIMINAL_RECORD" document
    And I am logged in as an administrator
    When I reject the "CRIMINAL_RECORD" document of "carlos.diaz@upc.edu.pe" with reason "The document is illegible"
    Then the response status should be 200
    And the response field "status" should be "REJECTED"
    And the response field "rejectionReason" should be "The document is illegible"
    And a "DocumentRejected" event should have been published
    And the driver "carlos.diaz@upc.edu.pe" should have status "PENDING_VERIFICATION"

  Scenario: Require a reason to reject a document
    Given I have uploaded a "CRIMINAL_RECORD" document
    And I am logged in as an administrator
    When I reject the "CRIMINAL_RECORD" document of "carlos.diaz@upc.edu.pe" without a reason
    Then the request should fail with status 400
    And the response should report errors for fields "reason"

  Scenario: Reject an invalid review decision
    Given I have uploaded a "CRIMINAL_RECORD" document
    And I am logged in as an administrator
    When I review the "CRIMINAL_RECORD" document of "carlos.diaz@upc.edu.pe" with decision "MAYBE"
    Then the request should fail with status 400

  Scenario: Reject reviewing a document twice
    Given I have uploaded a "DRIVERS_LICENSE" document
    And I am logged in as an administrator
    And I approve the "DRIVERS_LICENSE" document of "carlos.diaz@upc.edu.pe"
    When I reject the "DRIVERS_LICENSE" document of "carlos.diaz@upc.edu.pe" with reason "Changed my mind"
    Then the request should fail with status 409

  Scenario: Only administrators can review documents
    Given I have uploaded a "DRIVERS_LICENSE" document
    When I approve the "DRIVERS_LICENSE" document of "carlos.diaz@upc.edu.pe"
    Then the request should fail with status 403

  Scenario: Return not found when reviewing an unknown document
    Given I am logged in as an administrator
    When I approve the document with id "00000000-0000-0000-0000-000000999999"
    Then the request should fail with status 404
