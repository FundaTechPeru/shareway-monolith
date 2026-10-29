package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.application.AccessToken;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

  public static TokenResponse from(AccessToken token) {
    return new TokenResponse(token.value(), "Bearer", token.expiresInSeconds());
  }
}
