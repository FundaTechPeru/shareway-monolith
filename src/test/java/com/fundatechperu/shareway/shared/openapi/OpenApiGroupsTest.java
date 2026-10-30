package com.fundatechperu.shareway.shared.openapi;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
class OpenApiGroupsTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void swaggerUiListsOneGroupPerBoundedContext() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs/swagger-config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.urls[*].url").value(containsInAnyOrder("/v3/api-docs/booking")));
  }

  @Test
  void groupDocumentIsPublic() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs/" + OpenApiGroupsConfig.BOOKING))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi").exists());
  }

  @Test
  void swaggerUiIsPublic() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
  }
}
