package com.fundatechperu.shareway.drivermanagement.infrastructure.security;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Credentials of a user as seen by the login {@code AuthenticationManager}. */
public record UserAccountDetails(String email, String passwordHash, String role)
    implements UserDetails {

  public static UserAccountDetails from(User user) {
    return new UserAccountDetails(user.getEmail(), user.getPasswordHash(), user.getRole().name());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
