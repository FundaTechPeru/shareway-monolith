package com.fundatechperu.shareway.drivermanagement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void registersPassengerWithNormalizedData() {
    User user =
        User.registerPassenger(
            "  Ana.Torres@UPC.edu.pe ", "hash", " Ana Torres ", " +51987654321 ");

    assertThat(user.getEmail()).isEqualTo("ana.torres@upc.edu.pe");
    assertThat(user.getFullName()).isEqualTo("Ana Torres");
    assertThat(user.getPhone()).isEqualTo("+51987654321");
    assertThat(user.getRole()).isEqualTo(Role.PASSENGER);
    assertThat(user.getDriverStatus()).isNull();
    assertThat(user.getCreatedAt()).isNotNull();
  }

  @Test
  void normalizesEmailToTrimmedLowerCase() {
    assertThat(User.normalizeEmail(" Luis.Rojas@UPC.EDU.PE ")).isEqualTo("luis.rojas@upc.edu.pe");
  }

  @Test
  void passengerBecomesDriverPendingVerification() {
    User user = passenger();

    user.registerAsDriver("Q12345678", new EmergencyContact("Rosa Diaz", "+51911222333", "Mother"));

    assertThat(user.isDriver()).isTrue();
    assertThat(user.getRole()).isEqualTo(Role.DRIVER);
    assertThat(user.getDriverStatus()).isEqualTo(DriverStatus.PENDING_VERIFICATION);
    assertThat(user.getLicenseNumber()).isEqualTo("Q12345678");
    assertThat(user.getEmergencyContact().getName()).isEqualTo("Rosa Diaz");
  }

  @Test
  void driverCannotRegisterAgain() {
    User user = passenger();
    user.registerAsDriver("Q12345678", new EmergencyContact("Rosa Diaz", "+51911222333", "Mother"));

    assertThatThrownBy(
            () ->
                user.registerAsDriver(
                    "Z87654321", new EmergencyContact("Rosa Diaz", "+51911222333", "Mother")))
        .isInstanceOf(ConflictException.class);
    assertThat(user.getLicenseNumber()).isEqualTo("Q12345678");
  }

  private static User passenger() {
    return User.registerPassenger("carlos.diaz@upc.edu.pe", "hash", "Carlos Diaz", "+51987654321");
  }
}
