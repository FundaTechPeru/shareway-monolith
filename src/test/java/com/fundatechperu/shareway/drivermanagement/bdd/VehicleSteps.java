package com.fundatechperu.shareway.drivermanagement.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.time.Year;
import java.util.Map;

public class VehicleSteps {

  private static final String VEHICLES_PATH = "/api/v1/drivers/me/vehicles";

  private final ScenarioContext context;

  public VehicleSteps(ScenarioContext context) {
    this.context = context;
  }

  @Given("I have registered a vehicle with plate {string}")
  public void iHaveRegisteredAVehicle(String plate) {
    registerAndRemember(plate);
  }

  @Given("a driver {string} has registered a vehicle with plate {string}")
  public void aDriverHasRegisteredAVehicle(String email, String plate) {
    DriverSteps.registerAndLogInDriver(context, email);
    registerAndRemember(plate);
  }

  @When(
      "I register a vehicle with plate {string}, brand {string}, model {string},"
          + " year {int}, color {string} and {int} seats")
  public void iRegisterAVehicle(
      String plate, String brand, String model, int year, String color, int seats) {
    context.postJson(VEHICLES_PATH, vehicle(plate, brand, model, year, color, seats));
  }

  @When("I register a vehicle with plate {string} for next year's model")
  public void iRegisterAVehicleForNextYear(String plate) {
    context.postJson(
        VEHICLES_PATH, vehicle(plate, "Toyota", "Yaris", Year.now().getValue() + 1, "Red", 4));
  }

  @When("I register a vehicle with no data")
  public void iRegisterAVehicleWithNoData() {
    context.postJson(VEHICLES_PATH, Map.of());
  }

  @When("I list my vehicles")
  public void iListMyVehicles() {
    context.getRequest(VEHICLES_PATH);
  }

  @When("I view my vehicle with plate {string}")
  @When("I view the vehicle with plate {string}")
  public void iViewTheVehicle(String plate) {
    context.getRequest(VEHICLES_PATH + "/" + context.recallId(vehicleKey(plate)));
  }

  @When("I update my vehicle with plate {string} to color {string} and {int} seats")
  @When("I update the vehicle with plate {string} to color {string} and {int} seats")
  public void iUpdateTheVehicle(String plate, String color, int seats) {
    context.putJson(
        VEHICLES_PATH + "/" + context.recallId(vehicleKey(plate)),
        vehicle(plate, "Toyota", "Yaris", 2020, color, seats));
  }

  @When("I update my vehicle with plate {string} to plate {string}")
  public void iUpdateMyVehiclePlate(String plate, String newPlate) {
    context.putJson(
        VEHICLES_PATH + "/" + context.recallId(vehicleKey(plate)),
        vehicle(newPlate, "Toyota", "Yaris", 2020, "Red", 4));
  }

  private void registerAndRemember(String plate) {
    context.postJson(VEHICLES_PATH, vehicle(plate, "Toyota", "Yaris", 2020, "Red", 4));
    assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(201);
    context.rememberId(vehicleKey(context.read("$.plate")), context.lastId());
  }

  private static Map<String, Object> vehicle(
      String plate, String brand, String model, int year, String color, int seats) {
    return Map.of(
        "plate", plate, "brand", brand, "model", model, "year", year, "color", color, "seats",
        seats);
  }

  private static String vehicleKey(String plate) {
    return "vehicle:" + plate.toUpperCase();
  }
}
