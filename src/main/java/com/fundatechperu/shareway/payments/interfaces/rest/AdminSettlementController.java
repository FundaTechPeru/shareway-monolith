package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.SettlementService;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import com.fundatechperu.shareway.payments.interfaces.PaymentsExamples;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.SettlementResponse;
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
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Administración de las liquidaciones a conductores. */
@RestController
@RequestMapping("/api/v1/admin/settlements")
@Tag(
    name = "Admin Settlements",
    description = "Settlement payouts managed by administrators. Administrators only.")
public class AdminSettlementController {

  private final SettlementService settlementService;

  public AdminSettlementController(SettlementService settlementService) {
    this.settlementService = settlementService;
  }

  @GetMapping
  @Operation(
      summary = "List settlements, optionally filtered by status",
      description = "Every settlement of every driver, newest first.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Settlements",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = SettlementResponse.class)),
                examples = @ExampleObject(value = PaymentsExamples.SETTLEMENT_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Unknown status",
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
        description = "The user is not an administrator",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public List<SettlementResponse> list(
      @Parameter(
              description = "PENDING or PAID; omit it to list every settlement",
              example = "PENDING")
          @RequestParam
          Optional<SettlementStatus> status) {
    return settlementService.listAll(status).stream().map(SettlementResponse::from).toList();
  }

  @PatchMapping("/{id}/mark-paid")
  @Operation(
      summary = "Mark a pending settlement as paid",
      description =
          "Records that the platform paid the net amount to the driver: PENDING becomes PAID with"
              + " paidAt. A settlement is paid only once.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Settlement paid",
        content =
            @Content(
                schema = @Schema(implementation = SettlementResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.PAID_SETTLEMENT_RESPONSE))),
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
        description = "The user is not an administrator",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown settlement",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "409",
        description = "The settlement is already paid",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.SETTLEMENT_ALREADY_PAID)))
  })
  public SettlementResponse markPaid(
      @Parameter(description = "Settlement id", example = PaymentsExamples.SETTLEMENT_ID)
          @PathVariable
          UUID id) {
    return SettlementResponse.from(settlementService.markPaid(id));
  }
}
