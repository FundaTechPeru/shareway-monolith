package com.fundatechperu.shareway.payments.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Steps genéricos de autenticación, envío de peticiones HTTP y verificación de respuestas JSON. */
public class HttpSteps {

  private final MockMvc mockMvc;
  private final TestTokenFactory tokens;
  private final ScenarioContext context;
  private final String internalApiKey;

  public HttpSteps(
      MockMvc mockMvc,
      TestTokenFactory tokens,
      ScenarioContext context,
      @Value("${app.internal.api-key}") String internalApiKey) {
    this.mockMvc = mockMvc;
    this.tokens = tokens;
    this.context = context;
    this.internalApiKey = internalApiKey;
  }

  @Given("I am authenticated as a {word} with id {long}")
  public void authenticatedAs(String role, Long userId) {
    context.setBearerToken(tokens.token(TestIds.uuid(userId), role));
  }

  @Given("I am authenticated with an expired {word} token with id {long}")
  public void authenticatedWithExpiredToken(String role, Long userId) {
    context.setBearerToken(tokens.expiredToken(TestIds.uuid(userId), role));
  }

  @Given("I am authenticated with a token without role for user {long}")
  public void authenticatedWithoutRole(Long userId) {
    context.setBearerToken(tokens.tokenWithoutRole(TestIds.uuid(userId)));
  }

  @Given("I am not authenticated")
  public void notAuthenticated() {
    context.setBearerToken(null);
    context.setApiKey(null);
  }

  @Given("I use the internal API key")
  public void useInternalApiKey() {
    context.setApiKey(internalApiKey);
  }

  @Given("I use the internal API key {string}")
  public void useCustomApiKey(String apiKey) {
    context.setApiKey(apiKey);
  }

  @When("I send a {word} request to {string}")
  public void sendRequest(String method, String path) throws Exception {
    perform(method, path, null);
  }

  @When("I send a {word} request to {string} with body:")
  public void sendRequestWithBody(String method, String path, String body) throws Exception {
    perform(method, path, body);
  }

  @Then("the response status should be {int}")
  public void responseStatus(int status) throws Exception {
    assertThat(context.getLastResult().getResponse().getStatus())
        .as(context.getLastResult().getResponse().getContentAsString())
        .isEqualTo(status);
  }

  @Then("the response field {string} should be {string}")
  public void responseField(String path, String expected) throws Exception {
    Object actual = read(path);
    String resolved = context.resolve(expected);
    if (actual instanceof Number number) {
      assertThat(new BigDecimal(number.toString())).isEqualByComparingTo(new BigDecimal(resolved));
    } else {
      assertThat(String.valueOf(actual)).isEqualTo(resolved);
    }
  }

  @Then("the response field {string} should start with {string}")
  public void responseFieldStartsWith(String path, String prefix) throws Exception {
    assertThat(String.valueOf(read(path))).startsWith(prefix);
  }

  @Then("the response field {string} should not exist")
  public void responseFieldAbsent(String path) throws Exception {
    String body = context.getLastResult().getResponse().getContentAsString();
    try {
      assertThat((Object) JsonPath.read(body, "$." + path)).isNull();
    } catch (PathNotFoundException ignored) {
    }
  }

  @Then("the response should contain {int} items")
  public void responseItems(int count) throws Exception {
    List<?> items = JsonPath.read(context.getLastResult().getResponse().getContentAsString(), "$");
    assertThat(items).hasSize(count);
  }

  @Then("every item field {string} should be {string}")
  public void everyItemField(String field, String expected) throws Exception {
    List<Object> values =
        JsonPath.read(context.getLastResult().getResponse().getContentAsString(), "$[*]." + field);
    assertThat(values)
        .isNotEmpty()
        .allSatisfy(
            value -> assertThat(String.valueOf(value)).isEqualTo(context.resolve(expected)));
  }

  @Then("the response should report an error on field {string}")
  public void responseFieldError(String field) throws Exception {
    List<String> fields =
        JsonPath.read(
            context.getLastResult().getResponse().getContentAsString(), "$.fieldErrors[*].field");
    assertThat(fields).contains(field);
  }

  @Then("I save the response field {string} as {string}")
  public void saveResponseField(String path, String name) throws Exception {
    context.put(name, read(path));
  }

  private Object read(String path) throws Exception {
    return JsonPath.read(context.getLastResult().getResponse().getContentAsString(), "$." + path);
  }

  private void perform(String method, String path, String body) throws Exception {
    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.request(HttpMethod.valueOf(method), context.resolve(path));
    if (body != null) {
      request.contentType(MediaType.APPLICATION_JSON).content(context.resolve(body));
    }
    if (context.getBearerToken() != null) {
      request.header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getBearerToken());
    }
    if (context.getApiKey() != null) {
      request.header("X-Internal-Api-Key", context.getApiKey());
    }
    context.setLastResult(mockMvc.perform(request).andReturn());
  }
}
