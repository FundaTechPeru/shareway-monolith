package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.SettlementService;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.EarningsSummaryResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.SettlementResponse;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
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
  @Operation(summary = "List the settlements of the authenticated driver")
  public List<SettlementResponse> mySettlements(@AuthenticationPrincipal AuthenticatedUser user) {
    return settlementService.listDriverSettlements(user.id()).stream()
        .map(SettlementResponse::from)
        .toList();
  }

  @GetMapping("/summary")
  @Operation(summary = "Earnings totals of the authenticated driver")
  public EarningsSummaryResponse mySummary(@AuthenticationPrincipal AuthenticatedUser user) {
    return EarningsSummaryResponse.from(settlementService.summarize(user.id()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one settlement of the authenticated driver")
  public SettlementResponse mySettlement(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
    return SettlementResponse.from(settlementService.getDriverSettlement(user.id(), id));
  }
}
