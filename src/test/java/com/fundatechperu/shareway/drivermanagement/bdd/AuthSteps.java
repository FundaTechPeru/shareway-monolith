package com.fundatechperu.shareway.drivermanagement.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;

public class AuthSteps {

  private final ScenarioContext context;

  public AuthSteps(ScenarioContext context) {
    this.context = context;
  }

  @Given("a user is registered with email {string} and password {string}")
  public void aUserIsRegistered(String email, String password) {
    registerUser(context, email, password);
  }

  @Given("I am logged in as {string} with password {string}")
  public void iAmLoggedInAs(String email, String password) {
    logIn(context, email, password);
  }

  @When("I register with the following data:")
  public void iRegisterWith(Map<String, String> data) {
    context.postJson("/api/v1/auth/register", data);
  }

  @When("I log in with email {string} and password {string}")
  public void iLogInWith(String email, String password) {
    context.postJson("/api/v1/auth/login", Map.of("email", email, "password", password));
  }

  @When("I request my profile")
  public void iRequestMyProfile() {
    context.getRequest("/api/v1/users/me");
  }

  @When("I request my profile without a token")
  public void iRequestMyProfileWithoutToken() {
    context.clearToken();
    context.getRequest("/api/v1/users/me");
  }

  @When("I request my profile with the token {string}")
  public void iRequestMyProfileWithToken(String token) {
    context.useToken(token);
    context.getRequest("/api/v1/users/me");
  }

  @Then("I should receive a bearer access token")
  public void iShouldReceiveABearerAccessToken() {
    assertThat(context.lastStatus()).isEqualTo(200);
    assertThat((String) context.read("$.accessToken")).isNotBlank();
    assertThat((String) context.read("$.tokenType")).isEqualTo("Bearer");
    assertThat((Integer) context.read("$.expiresIn")).isEqualTo(3600);
  }

  static void registerUser(ScenarioContext context, String email, String password) {
    context.postJson(
        "/api/v1/auth/register",
        Map.of(
            "email",
            email,
            "password",
            password,
            "fullName",
            "Test User",
            "phone",
            "+51987654321"));
    assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(201);
  }

  static void logIn(ScenarioContext context, String email, String password) {
    context.clearToken();
    context.postJson("/api/v1/auth/login", Map.of("email", email, "password", password));
    assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(200);
    context.logIn(email, context.read("$.accessToken"));
  }
}
