package com.fundatechperu.shareway.booking;

import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.JwtService;
import com.fundatechperu.shareway.shared.security.UserRole;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base of the Booking &amp; Pricing integration tests. The standalone service reset a local
 * PostgreSQL with Flyway clean; the monolith runs on the shared H2 test database, so each test
 * deletes only the booking data of the bp schema and keeps the seeded fare rule.
 */
public abstract class AbstractBookingIntegrationTest {

  @Autowired protected JdbcTemplate jdbcTemplate;

  @Autowired private JwtService jwtService;

  /** Deletes the bp booking data, children first; fare_rules keeps the seeded rule version 1. */
  protected void deleteBookingData() {
    jdbcTemplate.update("DELETE FROM bp.fares");
    jdbcTemplate.update("DELETE FROM bp.penalties");
    jdbcTemplate.update("DELETE FROM bp.booking_sagas");
    jdbcTemplate.update("DELETE FROM bp.bookings");
    jdbcTemplate.update("DELETE FROM bp.seat_inventories");
  }

  /** Access token of a passenger, signed like the ones Driver Management issues at login. */
  protected String bearerToken(UUID passengerId) {
    return jwtService
        .issue(
            new AuthenticatedUser(
                passengerId, "passenger-" + passengerId + "@shareway.test", UserRole.PASSENGER))
        .value();
  }
}
