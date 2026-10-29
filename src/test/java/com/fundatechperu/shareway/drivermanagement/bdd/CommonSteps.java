package com.fundatechperu.shareway.drivermanagement.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import java.util.Arrays;
import java.util.List;

public class CommonSteps {

  private final ScenarioContext context;

  public CommonSteps(ScenarioContext context) {
    this.context = context;
  }

  @Then("the response status should be {int}")
  public void theResponseStatusShouldBe(int expectedStatus) {
    assertThat(context.lastStatus())
        .as("status of response %s", context.lastBody())
        .isEqualTo(expectedStatus);
  }

  @Then("the request should fail with status {int}")
  public void theRequestShouldFailWithStatus(int expectedStatus) {
    theResponseStatusShouldBe(expectedStatus);
    assertThat((Integer) context.read("$.status")).isEqualTo(expectedStatus);
    assertThat((String) context.read("$.timestamp")).isNotBlank();
    assertThat((String) context.read("$.error")).isNotBlank();
    assertThat((String) context.read("$.message")).isNotBlank();
    assertThat((String) context.read("$.path")).startsWith("/api/v1/");
    assertThat((List<?>) context.read("$.fieldErrors")).isNotNull();
  }

  @Then("the response field {string} should be {string}")
  public void theResponseFieldShouldBe(String field, String expected) {
    Object actual = context.read("$." + field);
    assertThat(String.valueOf(actual)).isEqualTo(expected);
  }

  @Then("the response should contain {int} items")
  public void theResponseShouldContainItems(int expectedCount) {
    List<?> items = context.read("$");
    assertThat(items).hasSize(expectedCount);
  }

  @Then("the response should report errors for fields {string}")
  public void theResponseShouldReportErrorsForFields(String fields) {
    List<String> reported = context.read("$.fieldErrors[*].field");
    String[] expected = Arrays.stream(fields.split(",")).map(String::trim).toArray(String[]::new);
    assertThat(reported).contains(expected);
  }
}
