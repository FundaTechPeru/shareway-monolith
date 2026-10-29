package com.fundatechperu.shareway.drivermanagement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VehicleTest {

  @ParameterizedTest
  @ValueSource(strings = {"abc123", "abc-123", " ABC-123 ", "AbC123"})
  void normalizesPlateToUpperCaseWithHyphen(String plate) {
    assertThat(Vehicle.normalizePlate(plate)).isEqualTo("ABC-123");
  }

  @ParameterizedTest
  @ValueSource(strings = {"AB12", "ABCD-123", "AB-1234", "AB_123", ""})
  void rejectsInvalidPlates(String plate) {
    assertThatThrownBy(() -> Vehicle.normalizePlate(plate))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void registerAndUpdateKeepTheNormalizedPlate() {
    Vehicle vehicle =
        Vehicle.register(new UUID(0, 1), "abc123", " Toyota ", "Yaris", 2020, "Red", 4);

    vehicle.update("def-456", "Toyota", "Yaris", 2021, " Blue ", 5);

    assertThat(vehicle.getPlate()).isEqualTo("DEF-456");
    assertThat(vehicle.getColor()).isEqualTo("Blue");
    assertThat(vehicle.getSeats()).isEqualTo(5);
    assertThat(vehicle.getDriverId()).isEqualTo(new UUID(0, 1));
  }
}
