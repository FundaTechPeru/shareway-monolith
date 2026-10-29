package com.fundatechperu.shareway.tripplanning.interfaces.rest.dto;

import com.fundatechperu.shareway.tripplanning.domain.model.MatchProposal;
import com.fundatechperu.shareway.tripplanning.domain.model.MatchProposalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchProposalResponse(
    UUID id,
    UUID driverId,
    BigDecimal estimatedFare,
    int estimatedDurationMinutes,
    MatchProposalStatus status,
    Instant createdAt) {

  public static MatchProposalResponse from(MatchProposal proposal) {
    return new MatchProposalResponse(
        proposal.getId(),
        proposal.getDriverId(),
        proposal.getEstimatedFare(),
        proposal.getEstimatedDurationMinutes(),
        proposal.getStatus(),
        proposal.getCreatedAt());
  }
}
