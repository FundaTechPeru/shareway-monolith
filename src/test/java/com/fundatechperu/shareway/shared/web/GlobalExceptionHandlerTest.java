package com.fundatechperu.shareway.shared.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void notFoundUsesTheUniformBody() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Thing not found"))
        .andExpect(jsonPath("$.path").value("/test/not-found"))
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void conflictIs409() throws Exception {
    mockMvc
        .perform(get("/test/conflict"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Already exists"));
  }

  @Test
  void domainValidationReportsTheField() throws Exception {
    mockMvc
        .perform(get("/test/domain-validation"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("plate"));
  }

  @Test
  void invalidBodyListsEveryInvalidField() throws Exception {
    mockMvc
        .perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
  }

  @Test
  void malformedBodyIs400() throws Exception {
    mockMvc
        .perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void malformedUuidInThePathIs400() throws Exception {
    mockMvc
        .perform(get("/test/items/not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("id"));
  }

  @Test
  void unexpectedErrorsDoNotLeakDetails() throws Exception {
    mockMvc
        .perform(get("/test/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }

  @RestController
  static class FailingController {

    @GetMapping("/test/not-found")
    void notFound() {
      throw new ResourceNotFoundException("Thing not found");
    }

    @GetMapping("/test/conflict")
    void conflict() {
      throw new ConflictException("Already exists");
    }

    @GetMapping("/test/domain-validation")
    void domainValidation() {
      throw new DomainValidationException("plate", "plate is invalid");
    }

    @PostMapping("/test/body")
    void body(@Valid @RequestBody NamedBody body) {}

    @GetMapping("/test/items/{id}")
    void item(@PathVariable UUID id) {}

    @GetMapping("/test/boom")
    void boom() {
      throw new IllegalStateException("database password is 1234");
    }
  }

  record NamedBody(@NotBlank String name) {}
}
