package com.fundatechperu.shareway.payments.bdd;

import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.Money;
import com.fundatechperu.shareway.payments.domain.policy.CommissionPolicy;
import com.fundatechperu.shareway.payments.domain.repository.DriverSettlementRepository;
import io.cucumber.java.en.Given;
import java.math.BigDecimal;
import java.time.Instant;

/** Steps que preparan liquidaciones de conductores directamente sobre el puerto del dominio. */
public class SettlementSteps {

  private final DriverSettlementRepository settlements;
  private final CommissionPolicy commissionPolicy;
  private final ScenarioContext context;

  public SettlementSteps(
      DriverSettlementRepository settlements,
      CommissionPolicy commissionPolicy,
      ScenarioContext context) {
    this.settlements = settlements;
    this.commissionPolicy = commissionPolicy;
    this.context = context;
  }

  @Given("driver {long} has a settlement for trip {long} with gross {string} PEN saved as {string}")
  public void driverHasSettlement(Long driverId, Long tripId, String gross, String name) {
    DriverSettlement settlement =
        settlements.save(
            DriverSettlement.create(
                TestIds.uuid(tripId),
                TestIds.uuid(driverId),
                Money.of(new BigDecimal(gross), Money.PEN),
                Instant.now(),
                commissionPolicy));
    context.put(name, settlement.getId());
  }
}
