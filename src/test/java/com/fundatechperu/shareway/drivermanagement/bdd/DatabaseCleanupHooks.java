package com.fundatechperu.shareway.drivermanagement.bdd;

import io.cucumber.java.Before;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Empties the Driver Management tables (schema dm) before every scenario. */
public class DatabaseCleanupHooks {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseCleanupHooks(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Before(order = 0)
  public void truncateAllTables() {
    List<String> tables =
        jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables"
                + " WHERE table_schema = 'dm' AND table_type = 'BASE TABLE'",
            String.class);
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
    tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE dm." + table));
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
  }
}
