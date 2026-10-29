package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

  private static final String INVALID_CREDENTIALS = "Invalid email or password";

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final AccessTokenProvider accessTokenProvider;

  public AuthenticationService(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      AccessTokenProvider accessTokenProvider) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.accessTokenProvider = accessTokenProvider;
  }

  @Transactional(readOnly = true)
  public AccessToken login(String email, String password) {
    String normalizedEmail = User.normalizeEmail(email);
    try {
      authenticationManager.authenticate(
          UsernamePasswordAuthenticationToken.unauthenticated(normalizedEmail, password));
    } catch (AuthenticationException ex) {
      throw new BadCredentialsException(INVALID_CREDENTIALS);
    }
    User user =
        userRepository
            .findByEmail(normalizedEmail)
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));
    return accessTokenProvider.issue(user);
  }
}
