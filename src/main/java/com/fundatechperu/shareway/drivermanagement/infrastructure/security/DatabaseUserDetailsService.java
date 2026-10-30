package com.fundatechperu.shareway.drivermanagement.infrastructure.security;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Loads the credentials checked at login; requests themselves are authenticated by the JWT. */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public DatabaseUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    return userRepository
        .findByEmail(User.normalizeEmail(username))
        .map(UserAccountDetails::from)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
