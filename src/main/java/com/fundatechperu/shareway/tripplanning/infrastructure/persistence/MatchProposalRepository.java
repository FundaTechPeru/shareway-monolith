package com.fundatechperu.shareway.tripplanning.infrastructure.persistence;

import com.fundatechperu.shareway.tripplanning.domain.model.MatchProposal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchProposalRepository extends JpaRepository<MatchProposal, UUID> {

  List<MatchProposal> findByTripRequestIdOrderByCreatedAtDesc(UUID tripRequestId);
}
