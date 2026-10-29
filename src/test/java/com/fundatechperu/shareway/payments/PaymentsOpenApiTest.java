package com.fundatechperu.shareway.payments;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentsOpenApiTest {

  private static final String AUTHORIZATIONS =
      "$.paths['/api/v1/internal/payments/authorizations']";

  @Autowired private MockMvc mockMvc;

  @Test
  void internalEndpointsUseTheApiKeyScheme() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs/payments"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.components.securitySchemes.internalApiKey.name")
                .value("X-Internal-Api-Key"))
        .andExpect(jsonPath(AUTHORIZATIONS + ".post.security[0].internalApiKey").isArray());
  }

  @Test
  void authorizationDocumentsDeclinedAndUnavailableGatewayWithExamples() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs/payments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath(AUTHORIZATIONS + ".post.requestBody.content.*.examples").exists())
        .andExpect(jsonPath(AUTHORIZATIONS + ".post.responses['402'].content.*.example").exists())
        .andExpect(jsonPath(AUTHORIZATIONS + ".post.responses['503'].content.*.example").exists());
  }
}
