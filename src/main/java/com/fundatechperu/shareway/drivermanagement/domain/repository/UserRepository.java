package com.fundatechperu.shareway.drivermanagement.domain.repository;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  User save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByLicenseNumber(String licenseNumber);
}
