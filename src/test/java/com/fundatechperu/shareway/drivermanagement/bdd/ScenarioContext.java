package com.fundatechperu.shareway.drivermanagement.bdd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.spring.ScenarioScope;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-scenario state shared by step definitions: the current bearer token and the last HTTP
 * response.
 */
@Component
@ScenarioScope
public class ScenarioContext {

  private final MockMvc mockMvc;
  private final JsonMapper jsonMapper;
  private final Map<String, UUID> rememberedIds = new HashMap<>();
  private String currentToken;
  private String currentEmail;
  private MvcResult lastResult;

  public ScenarioContext(MockMvc mockMvc, JsonMapper jsonMapper) {
    this.mockMvc = mockMvc;
    this.jsonMapper = jsonMapper;
  }

  public void logIn(String email, String token) {
    this.currentEmail = email;
    this.currentToken = token;
  }

  public void useToken(String token) {
    this.currentToken = token;
  }

  public void clearToken() {
    this.currentToken = null;
    this.currentEmail = null;
  }

  public String currentEmail() {
    return currentEmail;
  }

  public void rememberId(String key, UUID id) {
    rememberedIds.put(key, id);
  }

  public UUID recallId(String key) {
    UUID id = rememberedIds.get(key);
    if (id == null) {
      throw new IllegalStateException("No id remembered for " + key);
    }
    return id;
  }

  public UUID lastId() {
    return UUID.fromString(read("$.id"));
  }

  public MvcResult getRequest(String path) {
    return perform(get(path));
  }

  public MvcResult postJson(String path, Object body) {
    return perform(
        post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(body)));
  }

  public MvcResult putJson(String path, Object body) {
    return perform(
        put(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(body)));
  }

  public MvcResult patchJson(String path, Object body) {
    return perform(
        patch(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(body)));
  }

  public MvcResult perform(AbstractMockHttpServletRequestBuilder<?> request) {
    if (currentToken != null) {
      request.header(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken);
    }
    try {
      lastResult = mockMvc.perform(request).andReturn();
    } catch (Exception ex) {
      throw new IllegalStateException("Request failed", ex);
    }
    return lastResult;
  }

  public int lastStatus() {
    return lastResult.getResponse().getStatus();
  }

  public String lastBody() {
    try {
      return lastResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot read response body", ex);
    }
  }

  public <T> T read(String jsonPath) {
    return JsonPath.read(lastBody(), jsonPath);
  }
}
