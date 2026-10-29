package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

  private final SpringDataUserRepository jpaRepository;

  public UserRepositoryAdapter(SpringDataUserRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public User save(User user) {
    return jpaRepository.save(user);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByLicenseNumber(String licenseNumber) {
    return jpaRepository.existsByLicenseNumber(licenseNumber);
  }
}
