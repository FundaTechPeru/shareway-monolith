package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.AuthenticationService;
import com.fundatechperu.shareway.drivermanagement.application.UserRegistrationService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.LoginRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.RegisterUserRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.TokenResponse;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Account registration and login")
@SecurityRequirements
public class AuthController {

  private final UserRegistrationService userRegistrationService;
  private final AuthenticationService authenticationService;

  public AuthController(
      UserRegistrationService userRegistrationService,
      AuthenticationService authenticationService) {
    this.userRegistrationService = userRegistrationService;
    this.authenticationService = authenticationService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Register a passenger account",
      description =
          "Creates a user with role PASSENGER. Returns 409 if the email is already registered.")
  public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
    return UserResponse.from(userRegistrationService.register(request.toCommand()));
  }

  @PostMapping("/login")
  @Operation(
      summary = "Log in",
      description = "Returns a JWT bearer token. Returns 401 for invalid credentials.")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return TokenResponse.from(authenticationService.login(request.email(), request.password()));
  }
}
