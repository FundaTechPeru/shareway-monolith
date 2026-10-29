package com.fundatechperu.shareway.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that carry a valid {@code Authorization: Bearer} token.
 *
 * <p>An invalid or expired token is ignored, so public endpoints keep working and protected ones
 * answer 401 through the {@link SecurityErrorHandler}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final AuthenticatedUserResolver userResolver;
  private final WebAuthenticationDetailsSource detailsSource = new WebAuthenticationDetailsSource();

  public JwtAuthenticationFilter(JwtService jwtService, AuthenticatedUserResolver userResolver) {
    this.jwtService = jwtService;
    this.userResolver = userResolver;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null
        && header.startsWith(BEARER_PREFIX)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      jwtService
          .parse(header.substring(BEARER_PREFIX.length()).trim())
          .map(userResolver::resolve)
          .ifPresent(user -> authenticate(user, request));
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(AuthenticatedUser user, HttpServletRequest request) {
    UsernamePasswordAuthenticationToken authentication =
        UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities());
    authentication.setDetails(detailsSource.buildDetails(request));
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
