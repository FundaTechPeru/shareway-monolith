package com.fundatechperu.shareway.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT settings shared by every bounded context.
 *
 * @param secret HS256 secret (at least 32 bytes), read from {@code JWT_SECRET}
 * @param expirationMinutes lifetime of the issued access tokens
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {}
