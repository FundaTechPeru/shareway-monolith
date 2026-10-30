package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.SettlementService;
import com.fundatechperu.shareway.payments.interfaces.PaymentsExamples;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.EarningsSummaryResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.SettlementResponse;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ganancias y liquidaciones del conductor autenticado. */
@RestController
@RequestMapping("/api/v1/settlements/me")
@Tag(name = "Driver Earnings", description = "Settlements of the authenticated driver")
public class SettlementController {

  private final SettlementService settlementService;

  public SettlementController(SettlementService settlementService) {
    this.settlementService = settlementService;
  }

  @GetMapping
  @Operation(
      summary = "List the settlements of the authenticated driver",
      description =
          "One settlement per completed trip, newest first. Gross is what the passengers paid,"
              + " commission the platform share (15 % by default) and net what the driver gets.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Settlements of the driver",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = SettlementResponse.class)),
                examples = @ExampleObject(value = PaymentsExamples.SETTLEMENT_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public List<SettlementResponse> mySettlements(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return settlementService.listDriverSettlements(user.id()).stream()
        .map(SettlementResponse::from)
        .toList();
  }

  @GetMapping("/summary")
  @Operation(
      summary = "Earnings totals of the authenticated driver",
      description =
          "Sums every settlement of the driver and splits the net between what is still pending"
              + " and what the administrators already paid.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Earnings totals",
        content =
            @Content(
                schema = @Schema(implementation = EarningsSummaryResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.EARNINGS_SUMMARY_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public EarningsSummaryResponse mySummary(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return EarningsSummaryResponse.from(settlementService.summarize(user.id()));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get one settlement of the authenticated driver",
      description = "A settlement of another driver is reported as not found.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Settlement found",
        content =
            @Content(
                schema = @Schema(implementation = SettlementResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.SETTLEMENT_RESPONSE))),
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
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown settlement, or it belongs to another driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public SettlementResponse mySettlement(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
      @Parameter(description = "Settlement id", example = PaymentsExamples.SETTLEMENT_ID)
          @PathVariable
          UUID id) {
    return SettlementResponse.from(settlementService.getDriverSettlement(user.id(), id));
  }
}
