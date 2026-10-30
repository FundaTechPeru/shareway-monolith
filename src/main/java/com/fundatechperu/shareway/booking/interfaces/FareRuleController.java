package com.fundatechperu.shareway.booking.interfaces;

import com.fundatechperu.shareway.booking.pricing.application.dto.response.FareRuleResponse;
import com.fundatechperu.shareway.booking.pricing.application.service.FareRuleService;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only access to the fare-rule catalogue. The contract of the report exposes only GET: rules
 * are seeded by Flyway and are not created or modified through this API.
 */
@RestController
@RequestMapping("/api/v1/fare-rules")
@Tag(
    name = "Fare Rules",
    description = "Fare-rule catalogue used to price trips. Read-only: GET only.")
public class FareRuleController {

  private final FareRuleService service;

  public FareRuleController(FareRuleService service) {
    this.service = service;
  }

  @Operation(
      summary = "List fare rules",
      description = "Returns every fare rule known to the service, newest validity first.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Rules listed",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = FareRuleResponse.class)),
                examples = @ExampleObject(value = BookingExamples.FARE_RULE_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED)))
  })
  @GetMapping
  public List<FareRuleResponse> find() {
    return service.findAll().stream().map(FareRuleResponse::from).toList();
  }

  @Operation(
      summary = "Get a fare rule by id",
      description =
          "Returns one rule with its base amount, amount per km, platform fee and validity range.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Rule found",
        content =
            @Content(
                schema = @Schema(implementation = FareRuleResponse.class),
                examples = @ExampleObject(value = BookingExamples.FARE_RULE_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a UUID",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown rule id",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<FareRuleResponse> findById(
      @Parameter(description = "Fare rule identifier", example = BookingExamples.RULE_ID)
          @PathVariable
          UUID id) {
    return ResponseEntity.ok(FareRuleResponse.from(service.findById(id)));
  }
}
