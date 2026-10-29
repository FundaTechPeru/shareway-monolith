package com.fundatechperu.shareway.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SharewayBookingPricingApplicationTests extends AbstractBookingIntegrationTest {

  @Autowired private Flyway flyway;

  @Test
  void contextLoadsWithFlywayAndHibernateValidation() {
    List<String> scripts =
        Arrays.stream(flyway.info().applied()).map(MigrationInfo::getScript).toList();

    assertThat(scripts)
        .contains(
            "V4__create_booking_tables.sql",
            "V5__create_pricing_tables.sql",
            "V6__seed_booking_pricing_data.sql");
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bp.fare_rules WHERE version = 1", Integer.class))
        .isEqualTo(1);
  }
}
