package com.fundatechperu.shareway.booking.interfaces;

import com.fundatechperu.shareway.booking.pricing.application.dto.response.FareEstimateResponse;
import com.fundatechperu.shareway.booking.pricing.application.service.FareEstimateService;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fares/estimate")
@Tag(
    name = "Fares",
    description = "Fare estimation for shared trip groups. Read-only: nothing is persisted.")
public class FareEstimateController {

  private final FareEstimateService fareEstimateService;

  public FareEstimateController(FareEstimateService fareEstimateService) {
    this.fareEstimateService = fareEstimateService;
  }

  @Operation(
      summary = "Estimate the fare of a trip group",
      description =
          "Prices the group with the fare rule that is valid today (highest version whose"
              + " valid_from/valid_to range includes today): amount = ((base_amount +"
              + " amount_per_km * distance_km) / passengers) + platform fee, where passengers ="
              + " capacity - available_seats of the group (minimum 1) and the distance comes"
              + " from the trip group (currently a fake 12 km). The estimate does not create a"
              + " fares row.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Fare estimate computed",
        content =
            @Content(
                schema = @Schema(implementation = FareEstimateResponse.class),
                examples = @ExampleObject(value = BookingExamples.FARE_ESTIMATE_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "groupId is missing or is not a UUID",
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
        description = "Group not found in seat inventories, or no fare rule is valid today",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = BookingExamples.GROUP_NOT_FOUND)))
  })
  @GetMapping
  public FareEstimateResponse estimate(
      @Parameter(
              description = "Trip group identifier (seat_inventories.group_id)",
              required = true,
              example = BookingExamples.GROUP_ID)
          @RequestParam
          UUID groupId) {
    return fareEstimateService.estimate(groupId);
  }
}
