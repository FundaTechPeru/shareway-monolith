package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

  private final UserRepository userRepository;

  public UserProfileService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public User getProfile(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Transactional(readOnly = true)
  public User getDriverProfile(UUID userId) {
    User user = getProfile(userId);
    if (!user.isDriver()) {
      throw new ResourceNotFoundException("Driver profile not found");
    }
    return user;
  }
}
