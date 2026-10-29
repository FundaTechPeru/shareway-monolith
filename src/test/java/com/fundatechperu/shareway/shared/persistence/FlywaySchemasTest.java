package com.fundatechperu.shareway.shared.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywaySchemasTest {

  @Autowired private Flyway flyway;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void flywayCreatesOneSchemaPerBoundedContext() {
    List<String> schemas =
        jdbcTemplate.queryForList(
            "SELECT schema_name FROM information_schema.schemata", String.class);

    assertThat(schemas).contains("dm", "tpm", "bp", "ps");
  }

  @Test
  void flywayOwnsTheSchemaHistory() {
    assertThat(flyway.info().applied()).isNotEmpty();
    assertThat(flyway.info().applied()[0].getScript())
        .isEqualTo("V1__create_bounded_context_schemas.sql");
  }
}
