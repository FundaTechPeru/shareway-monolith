package com.fundatechperu.shareway.drivermanagement.infrastructure.config;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the administrator account given by {@code app.admin.email} and {@code app.admin.password}
 * on startup. Does nothing when either property is empty or the account already exists.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final String adminEmail;
  private final String adminPassword;

  public AdminAccountInitializer(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.admin.email:}") String adminEmail,
      @Value("${app.admin.password:}") String adminPassword) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (adminEmail.isBlank() || adminPassword.isBlank()) {
      return;
    }
    String email = User.normalizeEmail(adminEmail);
    if (userRepository.existsByEmail(email)) {
      return;
    }
    userRepository.save(
        User.createAdmin(
            email,
            passwordEncoder.encode(adminPassword),
            "ShareWay Administrator",
            "+51000000000"));
    log.info("Created administrator account {}", email);
  }
}
