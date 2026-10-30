package com.fundatechperu.shareway.shared.security;

/**
 * Optional hook to refresh the principal built from the token claims, for example to apply a role
 * change without logging in again.
 *
 * <p>When no bean implements it, the claims of the token are used as they are. The bounded
 * context that owns the users may provide one; once that context is extracted to its own service
 * the monolith falls back to the claims.
 */
@FunctionalInterface
public interface AuthenticatedUserResolver {

  /**
   * Returns the principal to use for the current request.
   *
   * @param fromToken user described by the token claims
   * @return the refreshed user, or {@code fromToken} when there is nothing to refresh
   */
  AuthenticatedUser resolve(AuthenticatedUser fromToken);
}
