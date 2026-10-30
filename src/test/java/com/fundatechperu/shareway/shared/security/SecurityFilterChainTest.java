package com.fundatechperu.shareway.shared.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFilterChainTest {

  private static final String PROTECTED_PATH = "/api/v1/not-mapped-yet";

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtService jwtService;

  @Test
  void healthEndpointIsPublic() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void requestWithoutTokenGetsTheUniformUnauthorizedBody() throws Exception {
    mockMvc
        .perform(get(PROTECTED_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.path").value(PROTECTED_PATH))
        .andExpect(jsonPath("$.fieldErrors").isArray());
  }

  @Test
  void invalidTokenIsUnauthorized() throws Exception {
    mockMvc
        .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void validTokenGoesThroughTheFilterChain() throws Exception {
    String token =
        jwtService
            .issue(new AuthenticatedUser(UUID.randomUUID(), "ana@upc.edu.pe", UserRole.PASSENGER))
            .value();

    mockMvc
        .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isNotFound());
  }
}
