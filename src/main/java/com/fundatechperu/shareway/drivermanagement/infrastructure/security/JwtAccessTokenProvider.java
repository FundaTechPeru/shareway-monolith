package com.fundatechperu.shareway.drivermanagement.infrastructure.security;

import com.fundatechperu.shareway.drivermanagement.application.AccessToken;
import com.fundatechperu.shareway.drivermanagement.application.AccessTokenProvider;
import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.IssuedToken;
import com.fundatechperu.shareway.shared.security.JwtService;
import com.fundatechperu.shareway.shared.security.UserRole;
import org.springframework.stereotype.Component;

/** Signs the access tokens with the shared {@link JwtService}. */
@Component
public class JwtAccessTokenProvider implements AccessTokenProvider {

  private final JwtService jwtService;

  public JwtAccessTokenProvider(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public AccessToken issue(User user) {
    IssuedToken token =
        jwtService.issue(
            new AuthenticatedUser(
                user.getId(), user.getEmail(), UserRole.valueOf(user.getRole().name())));
    return new AccessToken(token.value(), token.expiresInSeconds());
  }
}
