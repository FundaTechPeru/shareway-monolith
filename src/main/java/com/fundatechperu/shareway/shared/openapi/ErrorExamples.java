package com.fundatechperu.shareway.shared.openapi;

/** Example error bodies, shared by the OpenAPI documentation of every bounded context. */
public final class ErrorExamples {

  public static final String BAD_REQUEST =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 400,
        "error": "Bad Request",
        "message": "Validation failed",
        "path": "/api/v1/bookings",
        "fieldErrors": [{"field": "groupId", "message": "must not be null"}]
      }
      """;

  public static final String UNAUTHORIZED =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 401,
        "error": "Unauthorized",
        "message": "Authentication is required to access this resource",
        "path": "/api/v1/bookings",
        "fieldErrors": []
      }
      """;

  public static final String FORBIDDEN =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 403,
        "error": "Forbidden",
        "message": "You do not have permission to access this resource",
        "path": "/api/v1/fare-rules",
        "fieldErrors": []
      }
      """;

  public static final String NOT_FOUND =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 404,
        "error": "Not Found",
        "message": "Booking not found: 4c3b2a19-8f7e-4d6c-9b5a-1e2d3c4b5a69",
        "path": "/api/v1/bookings/4c3b2a19-8f7e-4d6c-9b5a-1e2d3c4b5a69",
        "fieldErrors": []
      }
      """;

  public static final String CONFLICT =
      """
      {
        "timestamp": "2026-10-29T14:05:00Z",
        "status": 409,
        "error": "Conflict",
        "message": "A booking already exists for requestId 6b1f0c2e-3d4a-4b5c-8d9e-0f1a2b3c4d5e",
        "path": "/api/v1/bookings",
        "fieldErrors": []
      }
      """;

  private ErrorExamples() {}
}
