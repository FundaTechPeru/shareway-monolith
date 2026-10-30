package com.fundatechperu.shareway.shared.openapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiConfigTest {

  @Autowired private MockMvc mockMvc;

  @ParameterizedTest
  @ValueSource(strings = {"booking"})
  void everyGroupCarriesTheInfoAndTheBearerScheme(String group) throws Exception {
    mockMvc
        .perform(get("/v3/api-docs/" + group))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("ShareWay Monolith"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
        .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
  }
}
