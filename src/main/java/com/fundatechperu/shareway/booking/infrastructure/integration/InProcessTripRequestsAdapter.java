package com.fundatechperu.shareway.booking.infrastructure.integration;

import com.fundatechperu.shareway.booking.domain.service.TripRequestsPort;
import com.fundatechperu.shareway.shared.integration.TripRequestDirectory;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * {@link TripRequestsPort} for the monolith: asks the {@link TripRequestDirectory} that Trip
 * Planning &amp; Matching offers in process. {@link HttpTripRequestsAdapter} takes its place when
 * {@code app.services.trip-planning-matching.url} is set.
 */
@Component
@ConditionalOnExpression("'${app.services.trip-planning-matching.url:}'.isBlank()")
public class InProcessTripRequestsAdapter implements TripRequestsPort {

  private final TripRequestDirectory directory;

  public InProcessTripRequestsAdapter(TripRequestDirectory directory) {
    this.directory = directory;
  }

  @Override
  public boolean belongsTo(UUID tripRequestId, UUID passengerId) {
    return directory.passengerOf(tripRequestId).map(passengerId::equals).orElse(false);
  }
}
