package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverAvailability;
import com.fundatechperu.shareway.drivermanagement.domain.repository.DriverAvailabilityRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvailabilityService {

  private final DriverAvailabilityRepository availabilityRepository;

  public AvailabilityService(DriverAvailabilityRepository availabilityRepository) {
    this.availabilityRepository = availabilityRepository;
  }

  @Transactional(readOnly = true)
  public List<DriverAvailability> getWeeklySchedule(UUID driverId) {
    return availabilityRepository.findAllByDriverId(driverId);
  }

  @Transactional
  public List<DriverAvailability> replaceWeeklySchedule(
      UUID driverId, List<AvailabilitySlotCommand> slots) {
    List<DriverAvailability> schedule =
        slots.stream()
            .map(
                slot ->
                    DriverAvailability.create(
                        driverId, slot.dayOfWeek(), slot.startTime(), slot.endTime()))
            .toList();
    DriverAvailability.ensureNoOverlaps(schedule);

    availabilityRepository.deleteAllByDriverId(driverId);
    return availabilityRepository.saveAll(schedule);
  }
}
