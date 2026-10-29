package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;

/** Port that signs the access token of a user who just logged in. */
public interface AccessTokenProvider {

  /** Issues a token whose subject is the user UUID and that carries the email and role. */
  AccessToken issue(User user);
}
