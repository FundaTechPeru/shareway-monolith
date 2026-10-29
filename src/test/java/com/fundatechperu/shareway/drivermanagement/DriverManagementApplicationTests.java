package com.fundatechperu.shareway.drivermanagement;

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
class DriverManagementApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void protectedEndpointReturnsUniformUnauthorizedError() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.path").value("/api/v1/users/me"))
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void openApiGroupDocumentsTheDriverManagementEndpoints() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs/drivermanagement"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("ShareWay Monolith"))
        .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post").exists())
        .andExpect(jsonPath("$.paths['/api/v1/drivers/me/vehicles'].post").exists())
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
  }
}
