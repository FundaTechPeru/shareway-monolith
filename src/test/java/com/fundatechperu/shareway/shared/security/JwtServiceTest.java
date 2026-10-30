package com.fundatechperu.shareway.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "unit-test-secret-key-with-at-least-32-bytes";
  private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  private static final AuthenticatedUser ANA =
      new AuthenticatedUser(
          UUID.fromString("7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f"),
          "ana.torres@upc.edu.pe",
          UserRole.DRIVER);

  private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 60));

  @Test
  void issuedTokenCarriesTheUserAndTheExpiration() {
    IssuedToken token = jwtService.issue(ANA);

    assertThat(token.expiresInSeconds()).isEqualTo(3600);
    assertThat(jwtService.parse(token.value())).contains(ANA);
  }

  @Test
  void subjectIsTheUserUuid() {
    String subject =
        Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(jwtService.issue(ANA).value())
            .getPayload()
            .getSubject();

    assertThat(subject).isEqualTo(ANA.id().toString());
  }

  @Test
  void userIdClaimIsTheUserUuidAsTheExtractedServicesExpect() {
    String userId =
        Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(jwtService.issue(ANA).value())
            .getPayload()
            .get("userId", String.class);

    assertThat(userId).isEqualTo(ANA.id().toString());
  }

  @Test
  void rejectsTokenSignedWithAnotherKey() {
    JwtService otherService =
        new JwtService(new JwtProperties("another-secret-key-with-at-least-32-bytes!", 60));

    assertThat(jwtService.parse(otherService.issue(ANA).value())).isEmpty();
  }

  @Test
  void rejectsExpiredAndMalformedTokens() {
    JwtService expiredService = new JwtService(new JwtProperties(SECRET, -1));

    assertThat(jwtService.parse(expiredService.issue(ANA).value())).isEmpty();
    assertThat(jwtService.parse("not-a-jwt")).isEmpty();
  }

  @Test
  void rejectsTokenWithoutRoleClaim() {
    String token =
        Jwts.builder()
            .subject(ANA.id().toString())
            .claim("email", ANA.email())
            .expiration(Date.from(Instant.now().plusSeconds(600)))
            .signWith(KEY)
            .compact();

    assertThat(jwtService.parse(token)).isEmpty();
  }

  @Test
  void rejectsTokenWhoseSubjectIsNotAUuid() {
    String token =
        Jwts.builder()
            .subject("ana.torres@upc.edu.pe")
            .claim("role", "DRIVER")
            .expiration(Date.from(Instant.now().plusSeconds(600)))
            .signWith(KEY)
            .compact();

    assertThat(jwtService.parse(token)).isEmpty();
  }

  @Test
  void rejectsUnknownRole() {
    String token =
        Jwts.builder()
            .subject(ANA.id().toString())
            .claim("role", "SUPERUSER")
            .expiration(Date.from(Instant.now().plusSeconds(600)))
            .signWith(KEY)
            .compact();

    assertThat(jwtService.parse(token)).isEmpty();
  }
}
