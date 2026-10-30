package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.model.EmergencyContact;
import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public User register(RegisterUserCommand command) {
    String email = User.normalizeEmail(command.email());
    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("Email is already registered");
    }
    User user =
        User.registerPassenger(
            email, passwordEncoder.encode(command.password()), command.fullName(), command.phone());
    return userRepository.save(user);
  }

  @Transactional
  public User registerDriver(UUID userId, RegisterDriverCommand command) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.isDriver()) {
      throw new ConflictException("User is already registered as a driver");
    }
    if (userRepository.existsByLicenseNumber(command.licenseNumber())) {
      throw new ConflictException("License number is already registered");
    }
    user.registerAsDriver(
        command.licenseNumber(),
        new EmergencyContact(
            command.emergencyContactName(),
            command.emergencyContactPhone(),
            command.emergencyContactRelationship()));
    return userRepository.save(user);
  }
}
