package com.fundatechperu.shareway.drivermanagement.infrastructure.security;

import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.AuthenticatedUserResolver;
import com.fundatechperu.shareway.shared.security.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Reloads the user of every request, so a role change (for example PASSENGER to DRIVER) applies
 * without logging in again.
 *
 * <p>A user that is not stored here keeps the claims of its token. When {@code
 * app.services.driver-management.url} points to the extracted service, {@link
 * com.fundatechperu.shareway.shared.security.DriverManagementUserResolver} takes its place.
 */
@Component
@ConditionalOnExpression("'${app.services.driver-management.url:}'.isBlank()")
public class CurrentUserResolver implements AuthenticatedUserResolver {

  private final UserRepository userRepository;

  public CurrentUserResolver(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public AuthenticatedUser resolve(AuthenticatedUser fromToken) {
    return userRepository
        .findById(fromToken.id())
        .map(
            user ->
                new AuthenticatedUser(
                    user.getId(), user.getEmail(), UserRole.valueOf(user.getRole().name())))
        .orElse(fromToken);
  }
}
