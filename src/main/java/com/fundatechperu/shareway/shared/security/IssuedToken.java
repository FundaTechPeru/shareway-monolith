package com.fundatechperu.shareway.shared.security;

/**
 * Signed access token returned to the client after a successful login.
 *
 * @param value compact JWT
 * @param expiresInSeconds lifetime of the token in seconds
 */
public record IssuedToken(String value, long expiresInSeconds) {}
