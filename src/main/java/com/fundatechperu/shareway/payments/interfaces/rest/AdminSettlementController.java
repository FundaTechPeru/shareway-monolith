package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.SettlementService;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.SettlementResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Admin Settlements", description = "Settlement payouts managed by administrators")
public class AdminSettlementController {

  private final SettlementService settlementService;

  public AdminSettlementController(SettlementService settlementService) {
    this.settlementService = settlementService;
  }

  @GetMapping
  @Operation(summary = "List settlements, optionally filtered by status")
  public List<SettlementResponse> list(@RequestParam Optional<SettlementStatus> status) {
    return settlementService.listAll(status).stream().map(SettlementResponse::from).toList();
  }

  @PatchMapping("/{id}/mark-paid")
  @Operation(summary = "Mark a pending settlement as paid")
  public SettlementResponse markPaid(@PathVariable UUID id) {
    return SettlementResponse.from(settlementService.markPaid(id));
  }
}
