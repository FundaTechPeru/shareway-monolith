package com.fundatechperu.shareway.tripplanning.infrastructure.integration;

import com.fundatechperu.shareway.shared.integration.TripRequestDirectory;
import com.fundatechperu.shareway.tripplanning.domain.model.TripRequest;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.TripRequestRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Answers the {@link TripRequestDirectory} query from the trip requests stored in this context. */
@Component
public class TripRequestDirectoryAdapter implements TripRequestDirectory {

  private final TripRequestRepository tripRequests;

  public TripRequestDirectoryAdapter(TripRequestRepository tripRequests) {
    this.tripRequests = tripRequests;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UUID> passengerOf(UUID tripRequestId) {
    return tripRequests.findById(tripRequestId).map(TripRequest::getPassengerId);
  }
}
