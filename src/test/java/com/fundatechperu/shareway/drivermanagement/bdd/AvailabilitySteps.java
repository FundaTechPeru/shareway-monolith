package com.fundatechperu.shareway.drivermanagement.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilitySteps {

  private static final String AVAILABILITY_PATH = "/api/v1/drivers/me/availability";

  private final ScenarioContext context;

  public AvailabilitySteps(ScenarioContext context) {
    this.context = context;
  }

  @Given("my weekly availability is:")
  public void myWeeklyAvailabilityIs(List<Map<String, String>> slots) {
    iSetMyWeeklyAvailabilityTo(slots);
    assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(200);
  }

  @When("I set my weekly availability to:")
  public void iSetMyWeeklyAvailabilityTo(List<Map<String, String>> slots) {
    context.putJson(AVAILABILITY_PATH, Map.of("slots", slots));
  }

  @When("I set my weekly availability to an empty list")
  public void iSetMyWeeklyAvailabilityToAnEmptyList() {
    context.putJson(AVAILABILITY_PATH, Map.of("slots", List.of()));
  }

  @When("I set my weekly availability to a slot on {string} from {string} to {string}")
  public void iSetMyWeeklyAvailabilityToASlot(String dayOfWeek, String startTime, String endTime) {
    context.putJson(
        AVAILABILITY_PATH,
        Map.of(
            "slots",
            List.of(Map.of("dayOfWeek", dayOfWeek, "startTime", startTime, "endTime", endTime))));
  }

  @When("I set my weekly availability to a slot on {string} without times")
  public void iSetMyWeeklyAvailabilityToASlotWithoutTimes(String dayOfWeek) {
    Map<String, Object> slot = new HashMap<>();
    slot.put("dayOfWeek", dayOfWeek);
    context.putJson(AVAILABILITY_PATH, Map.of("slots", List.of(slot)));
  }

  @When("I send a weekly availability without slots")
  public void iSendAWeeklyAvailabilityWithoutSlots() {
    context.putJson(AVAILABILITY_PATH, Map.of());
  }

  @When("I request my weekly availability")
  public void iRequestMyWeeklyAvailability() {
    context.getRequest(AVAILABILITY_PATH);
  }

  @Then("my availability should have {int} slot(s)")
  public void myAvailabilityShouldHaveSlots(int expectedCount) {
    List<?> slots = context.read("$.slots");
    assertThat(slots).hasSize(expectedCount);
  }

  @Then("slot {int} should be {string} from {string} to {string}")
  public void slotShouldBe(int position, String dayOfWeek, String startTime, String endTime) {
    String slot = "$.slots[" + (position - 1) + "]";
    assertThat((String) context.read(slot + ".dayOfWeek")).isEqualTo(dayOfWeek);
    assertThat((String) context.read(slot + ".startTime")).isEqualTo(startTime);
    assertThat((String) context.read(slot + ".endTime")).isEqualTo(endTime);
  }
}
