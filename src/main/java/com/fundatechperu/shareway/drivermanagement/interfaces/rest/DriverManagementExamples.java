package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

/** Request and response examples of the Driver Management OpenAPI document. */
final class DriverManagementExamples {

  static final String USER_ID = "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f";
  static final String DOCUMENT_ID = "6b1f0c2e-3d4a-4b5c-8d9e-0f1a2b3c4d5e";
  static final String VEHICLE_ID = "4c3b2a19-8f7e-4d6c-9b5a-1e2d3c4b5a69";

  static final String REGISTER_USER_REQUEST =
      """
      {
        "email": "ana.torres@upc.edu.pe",
        "password": "Secret123",
        "fullName": "Ana Torres",
        "phone": "+51987654321"
      }
      """;

  static final String USER_RESPONSE =
      """
      {
        "id": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "email": "ana.torres@upc.edu.pe",
        "fullName": "Ana Torres",
        "phone": "+51987654321",
        "role": "PASSENGER",
        "driverStatus": null,
        "createdAt": "2026-10-29T14:05:00Z"
      }
      """;

  static final String LOGIN_REQUEST =
      """
      {"email": "ana.torres@upc.edu.pe", "password": "Secret123"}
      """;

  static final String TOKEN_RESPONSE =
      """
      {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3ZDlmM2E1MiJ9.c2lnbmF0dXJl",
        "tokenType": "Bearer",
        "expiresIn": 3600
      }
      """;

  static final String INVALID_CREDENTIALS =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 401,
        "error": "Unauthorized",
        "message": "Invalid email or password",
        "path": "/api/v1/auth/login",
        "fieldErrors": []
      }
      """;

  static final String REGISTER_DRIVER_REQUEST =
      """
      {
        "licenseNumber": "Q12345678",
        "emergencyContact": {"name": "Rosa Diaz", "phone": "+51911222333", "relationship": "Mother"}
      }
      """;

  static final String DRIVER_PROFILE_RESPONSE =
      """
      {
        "userId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "fullName": "Ana Torres",
        "email": "ana.torres@upc.edu.pe",
        "licenseNumber": "Q12345678",
        "driverStatus": "PENDING_VERIFICATION",
        "emergencyContact": {"name": "Rosa Diaz", "phone": "+51911222333", "relationship": "Mother"}
      }
      """;

  static final String DOCUMENT_RESPONSE =
      """
      {
        "id": "6b1f0c2e-3d4a-4b5c-8d9e-0f1a2b3c4d5e",
        "driverId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "type": "DRIVERS_LICENSE",
        "status": "PENDING",
        "originalFilename": "license.pdf",
        "contentType": "application/pdf",
        "sizeBytes": 248331,
        "rejectionReason": null,
        "uploadedAt": "2026-10-29T14:05:00Z",
        "reviewedAt": null
      }
      """;

  static final String DOCUMENT_LIST_RESPONSE = "[" + DOCUMENT_RESPONSE + "]";

  static final String REVIEW_REQUEST =
      """
      {"decision": "REJECTED", "reason": "The document is illegible"}
      """;

  static final String REVIEWED_DOCUMENT_RESPONSE =
      """
      {
        "id": "6b1f0c2e-3d4a-4b5c-8d9e-0f1a2b3c4d5e",
        "driverId": "7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f",
        "type": "DRIVERS_LICENSE",
        "status": "REJECTED",
        "originalFilename": "license.pdf",
        "contentType": "application/pdf",
        "sizeBytes": 248331,
        "rejectionReason": "The document is illegible",
        "uploadedAt": "2026-10-29T14:05:00Z",
        "reviewedAt": "2026-10-29T16:20:00Z"
      }
      """;

  static final String DOCUMENT_ALREADY_REVIEWED =
      """
      {
        "timestamp": "2026-10-29T16:21:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "Document has already been reviewed",
        "path": "/api/v1/driver-documents/6b1f0c2e-3d4a-4b5c-8d9e-0f1a2b3c4d5e/review",
        "fieldErrors": []
      }
      """;

  static final String VEHICLE_REQUEST =
      """
      {
        "plate": "ABC-123",
        "brand": "Toyota",
        "model": "Yaris",
        "year": 2020,
        "color": "Red",
        "seats": 4
      }
      """;

  static final String VEHICLE_RESPONSE =
      """
      {
        "id": "4c3b2a19-8f7e-4d6c-9b5a-1e2d3c4b5a69",
        "plate": "ABC-123",
        "brand": "Toyota",
        "model": "Yaris",
        "year": 2020,
        "color": "Red",
        "seats": 4,
        "createdAt": "2026-10-29T14:05:00Z",
        "updatedAt": "2026-10-29T14:05:00Z"
      }
      """;

  static final String VEHICLE_LIST_RESPONSE = "[" + VEHICLE_RESPONSE + "]";

  static final String PLATE_TAKEN =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "Plate is already registered",
        "path": "/api/v1/drivers/me/vehicles",
        "fieldErrors": []
      }
      """;

  static final String AVAILABILITY_REQUEST =
      """
      {
        "slots": [
          {"dayOfWeek": "MONDAY", "startTime": "07:00", "endTime": "09:00"},
          {"dayOfWeek": "WEDNESDAY", "startTime": "17:30", "endTime": "19:00"}
        ]
      }
      """;

  static final String AVAILABILITY_RESPONSE = AVAILABILITY_REQUEST;

  static final String OVERLAPPING_SLOTS =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 400,
        "error": "Bad Request",
        "message": "Slots overlap on MONDAY: 07:00-09:00 and 08:30-10:00",
        "path": "/api/v1/drivers/me/availability",
        "fieldErrors": [
          {"field": "slots", "message": "Slots overlap on MONDAY: 07:00-09:00 and 08:30-10:00"}
        ]
      }
      """;

  private DriverManagementExamples() {}
}
