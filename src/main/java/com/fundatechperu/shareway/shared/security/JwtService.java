package com.fundatechperu.shareway.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Issues and validates the HS256 access tokens used by every bounded context.
 *
 * <p>Claims: {@code sub} and {@code userId} are the user UUID, {@code email} the user email and
 * {@code role} one of {@link UserRole}, the same claims the extracted Driver Management service
 * issues ({@code userId} is the one Payments &amp; Settlement reads). A token without a valid UUID
 * subject or role is rejected.
 */
@Service
public class JwtService {

  static final String USER_ID_CLAIM = "userId";
  static final String EMAIL_CLAIM = "email";
  static final String ROLE_CLAIM = "role";

  private final SecretKey signingKey;
  private final Duration expiration;

  public JwtService(JwtProperties properties) {
    this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    this.expiration = Duration.ofMinutes(properties.expirationMinutes());
  }

  /** Signs a token for the given user, valid for the configured number of minutes. */
  public IssuedToken issue(AuthenticatedUser user) {
    Instant now = Instant.now();
    String token =
        Jwts.builder()
            .subject(user.id().toString())
            .claim(USER_ID_CLAIM, user.id().toString())
            .claim(EMAIL_CLAIM, user.email())
            .claim(ROLE_CLAIM, user.role().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expiration)))
            .signWith(signingKey)
            .compact();
    return new IssuedToken(token, expiration.toSeconds());
  }

  /**
   * Verifies the signature and expiration of a token and returns the user it describes.
   *
   * @return the user, or empty when the token is invalid, expired or misses a required claim
   */
  public Optional<AuthenticatedUser> parse(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      String role = claims.get(ROLE_CLAIM, String.class);
      if (claims.getSubject() == null || role == null) {
        return Optional.empty();
      }
      return Optional.of(
          new AuthenticatedUser(
              UUID.fromString(claims.getSubject()),
              claims.get(EMAIL_CLAIM, String.class),
              UserRole.valueOf(role)));
    } catch (JwtException | IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
