package com.fundatechperu.shareway.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protects the service-to-service endpoints under {@code /api/v1/internal/**} with the {@code
 * X-Internal-Api-Key} header, compared in constant time. They never accept a user token, and the
 * API gateway does not expose them.
 */
public class InternalApiKeyFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Internal-Api-Key";
  public static final String INTERNAL_ROLE = "INTERNAL_SERVICE";
  public static final String INTERNAL_PATHS = "/api/v1/internal/**";

  private static final String INTERNAL_PREFIX = "/api/v1/internal/";

  private final byte[] expectedKey;
  private final SecurityErrorHandler errorHandler;

  public InternalApiKeyFilter(String expectedKey, SecurityErrorHandler errorHandler) {
    this.expectedKey = expectedKey.getBytes(StandardCharsets.UTF_8);
    this.errorHandler = errorHandler;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String provided = request.getHeader(HEADER);
    if (provided == null
        || !MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8))) {
      errorHandler.write(
          request, response, HttpStatus.UNAUTHORIZED, "Missing or invalid internal API key");
      return;
    }
    UsernamePasswordAuthenticationToken authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            "internal-service", null, List.of(new SimpleGrantedAuthority("ROLE_" + INTERNAL_ROLE)));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith(request.getContextPath() + INTERNAL_PREFIX);
  }
}
