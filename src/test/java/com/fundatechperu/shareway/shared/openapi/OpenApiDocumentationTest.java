package com.fundatechperu.shareway.shared.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Every bounded context serves its OpenAPI document, and every operation in it is documented:
 * summary, description, a success answer, 401 when it needs a token and an example for each JSON
 * request and response.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

  private static final List<String> HTTP_METHODS = List.of("get", "post", "put", "patch", "delete");

  @Autowired private MockMvc mockMvc;

  static Stream<Arguments> groups() {
    return Stream.of(
        Arguments.of(
            "tripplanning",
            List.of("/api/v1/trip-requests", "/api/v1/driver-availabilities", "/api/v1/info")),
        Arguments.of(
            "booking", List.of("/api/v1/bookings", "/api/v1/fares/", "/api/v1/fare-rules")),
        Arguments.of(
            "payments",
            List.of(
                "/api/v1/payments/",
                "/api/v1/payment-methods",
                "/api/v1/settlements/",
                "/api/v1/admin/settlements",
                "/api/v1/internal/payments/",
                "/api/v1/internal/events/")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("groups")
  void everyContextServesItsDocumentWithTheBearerScheme(String group, List<String> prefixes)
      throws Exception {
    String document = document(group);

    assertThat((String) JsonPath.read(document, "$.info.title")).isEqualTo("ShareWay Monolith");
    assertThat((String) JsonPath.read(document, "$.components.securitySchemes.bearerAuth.scheme"))
        .isEqualTo("bearer");
    Map<String, Object> paths = JsonPath.read(document, "$.paths");
    assertThat(paths).isNotEmpty();
    assertThat(paths.keySet()).allSatisfy(path -> assertThat(prefixes).anyMatch(path::startsWith));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("groups")
  @SuppressWarnings("unchecked")
  void everyOperationIsDocumentedWithResponsesAndExamples(String group, List<String> prefixes)
      throws Exception {
    Map<String, Map<String, Object>> paths = JsonPath.read(document(group), "$.paths");
    List<String> problems = new ArrayList<>();

    paths.forEach(
        (path, item) ->
            HTTP_METHODS.stream()
                .filter(item::containsKey)
                .forEach(
                    method ->
                        checkOperation(
                            method.toUpperCase() + " " + path,
                            (Map<String, Object>) item.get(method),
                            problems)));

    assertThat(problems).as("undocumented parts of the %s document", group).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private static void checkOperation(
      String operation, Map<String, Object> definition, List<String> problems) {
    if (isBlank(definition.get("summary")) || isBlank(definition.get("description"))) {
      problems.add(operation + ": summary or description missing");
    }
    Map<String, Object> responses = (Map<String, Object>) definition.get("responses");
    if (responses.keySet().stream().noneMatch(code -> code.startsWith("2"))) {
      problems.add(operation + ": no success response");
    }
    boolean isPublic = definition.get("security") instanceof List<?> security && security.isEmpty();
    if (!isPublic && !responses.containsKey("401")) {
      problems.add(operation + ": 401 not documented");
    }
    responses.forEach(
        (code, response) -> {
          Object content = ((Map<String, Object>) response).get("content");
          if (content instanceof Map<?, ?> media && !hasExamples(media)) {
            problems.add(operation + ": response " + code + " without example");
          }
        });
    if (definition.get("requestBody") instanceof Map<?, ?> body
        && body.get("content") instanceof Map<?, ?> media
        && media.containsKey("application/json")
        && !hasExamples(media)) {
      problems.add(operation + ": request body without example");
    }
  }

  private static boolean hasExamples(Map<?, ?> content) {
    return content.values().stream()
        .allMatch(
            mediaType ->
                mediaType instanceof Map<?, ?> type
                    && (type.containsKey("example") || type.containsKey("examples")));
  }

  private static boolean isBlank(Object value) {
    return !(value instanceof String text) || text.isBlank();
  }

  private String document(String group) throws Exception {
    return mockMvc
        .perform(get("/v3/api-docs/" + group))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}
