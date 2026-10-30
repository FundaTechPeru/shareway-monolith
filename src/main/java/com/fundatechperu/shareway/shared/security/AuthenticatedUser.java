package com.fundatechperu.shareway.shared.security;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Principal of an authenticated request, built from the JWT claims: {@code sub} is the user UUID.
 *
 * @param id user identifier, the same UUID every bounded context uses to reference the user
 * @param email email of the user
 * @param role current role of the user
 */
public record AuthenticatedUser(UUID id, String email, UserRole role) {

  /** Spring Security authorities of this principal, one {@code ROLE_} authority per role. */
  public List<GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }
}
