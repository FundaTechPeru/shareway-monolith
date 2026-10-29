package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.AuthenticationService;
import com.fundatechperu.shareway.drivermanagement.application.UserRegistrationService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.LoginRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.RegisterUserRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.TokenResponse;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.UserResponse;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Authentication", description = "Account registration and login. Public endpoints.")
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
          "Creates a user with role PASSENGER. The email is stored in lower case and must be"
              + " unique; the password needs 8 to 72 characters with at least one letter and one"
              + " number and is stored as a BCrypt hash.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "passenger",
                              value = DriverManagementExamples.REGISTER_USER_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Account created",
        content =
            @Content(
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.USER_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid email, password, full name or phone",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "409",
        description = "The email is already registered",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.CONFLICT)))
  })
  public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
    return UserResponse.from(userRegistrationService.register(request.toCommand()));
  }

  @PostMapping("/login")
  @Operation(
      summary = "Log in",
      description =
          "Checks the email and password and returns a bearer JWT valid for 60 minutes. The token"
              + " subject is the user UUID and it carries the email and role claims; send it as"
              + " Authorization: Bearer <accessToken> to every other endpoint.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "credentials",
                              value = DriverManagementExamples.LOGIN_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Credentials accepted",
        content =
            @Content(
                schema = @Schema(implementation = TokenResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.TOKEN_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Email or password missing or malformed",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Wrong email or password",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.INVALID_CREDENTIALS)))
  })
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return TokenResponse.from(authenticationService.login(request.email(), request.password()));
  }
}
