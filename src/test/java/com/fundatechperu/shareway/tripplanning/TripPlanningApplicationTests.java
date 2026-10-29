package com.fundatechperu.shareway.tripplanning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Imported from shareway-trip-planning-matching-service: the context starts on the tpm schema. */
@SpringBootTest
@ActiveProfiles("test")
class TripPlanningApplicationTests {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void contextLoadsWithTheTripPlanningSchema() {
    assertThat(
            jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'tpm'",
                String.class))
        .contains("trip_requests", "driver_availabilities", "match_proposals");
  }
}
