package com.fundatechperu.shareway.tripplanning.infrastructure.persistence;

import com.fundatechperu.shareway.tripplanning.domain.model.TripRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRequestRepository extends JpaRepository<TripRequest, UUID> {

  List<TripRequest> findAllByOrderByCreatedAtDesc();
}
