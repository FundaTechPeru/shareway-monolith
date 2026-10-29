package com.fundatechperu.shareway.drivermanagement.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import com.fundatechperu.shareway.drivermanagement.infrastructure.storage.StorageProperties;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.FileSystemUtils;

public class DocumentSteps {

  private static final String DOCUMENTS_PATH = "/api/v1/drivers/me/documents";
  private static final String ADMIN_EMAIL = "admin@shareway.pe";
  private static final byte[] PDF_HEADER = "%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII);
  private static final byte[] JPEG_HEADER = {
    (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10
  };
  private static final byte[] PNG_HEADER = {
    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
  };

  private final ScenarioContext context;
  private final RecordedDomainEvents recordedEvents;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final StorageProperties storageProperties;

  public DocumentSteps(
      ScenarioContext context,
      RecordedDomainEvents recordedEvents,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      StorageProperties storageProperties) {
    this.context = context;
    this.recordedEvents = recordedEvents;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.storageProperties = storageProperties;
  }

  @Before
  public void resetEventsAndStorage() throws IOException {
    recordedEvents.clear();
    FileSystemUtils.deleteRecursively(Path.of(storageProperties.path()));
  }

  @Given("I am logged in as an administrator")
  public void iAmLoggedInAsAnAdministrator() {
    if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
      userRepository.save(
          User.createAdmin(
              ADMIN_EMAIL,
              passwordEncoder.encode(DriverSteps.DEFAULT_PASSWORD),
              "ShareWay Administrator",
              "+51900000000"));
    }
    AuthSteps.logIn(context, ADMIN_EMAIL, DriverSteps.DEFAULT_PASSWORD);
  }

  @Given("I have uploaded a {string} document")
  public void iHaveUploadedADocument(String type) {
    uploadAndRemember(context.currentEmail(), type);
  }

  @Given("a driver {string} has uploaded a {string} document")
  public void aDriverHasUploadedADocument(String email, String type) {
    DriverSteps.registerAndLogInDriver(context, email);
    uploadAndRemember(email, type);
  }

  @When("I upload a {string} document named {string} of type {string}")
  public void iUploadADocument(String type, String filename, String contentType) {
    upload(type, filename, contentType, sampleContent(contentType));
  }

  @When("I upload a {string} document named {string} of type {string} with text content")
  public void iUploadADocumentWithTextContent(String type, String filename, String contentType) {
    upload(type, filename, contentType, "just some text".getBytes(StandardCharsets.UTF_8));
  }

  @When("I upload an empty {string} document")
  public void iUploadAnEmptyDocument(String type) {
    upload(type, "empty.pdf", "application/pdf", new byte[0]);
  }

  @When("I upload a {string} document larger than 5 MB")
  public void iUploadADocumentLargerThan5Mb(String type) {
    byte[] content = new byte[5 * 1024 * 1024 + 1];
    System.arraycopy(PDF_HEADER, 0, content, 0, PDF_HEADER.length);
    upload(type, "large.pdf", "application/pdf", content);
  }

  @When("I list my documents")
  public void iListMyDocuments() {
    context.getRequest(DOCUMENTS_PATH);
  }

  @When("I view my {string} document")
  public void iViewMyDocument(String type) {
    context.getRequest(
        DOCUMENTS_PATH + "/" + context.recallId(documentKey(context.currentEmail(), type)));
  }

  @When("I view the {string} document uploaded by {string}")
  public void iViewTheDocumentUploadedBy(String type, String email) {
    context.getRequest(DOCUMENTS_PATH + "/" + context.recallId(documentKey(email, type)));
  }

  @When("I approve the {string} document of {string}")
  public void iApproveTheDocumentOf(String type, String email) {
    review(context.recallId(documentKey(email, type)), "APPROVED", null);
  }

  @When("I reject the {string} document of {string} with reason {string}")
  public void iRejectTheDocumentOfWithReason(String type, String email, String reason) {
    review(context.recallId(documentKey(email, type)), "REJECTED", reason);
  }

  @When("I reject the {string} document of {string} without a reason")
  public void iRejectTheDocumentOfWithoutReason(String type, String email) {
    review(context.recallId(documentKey(email, type)), "REJECTED", null);
  }

  @When("I review the {string} document of {string} with decision {string}")
  public void iReviewTheDocumentWithDecision(String type, String email, String decision) {
    review(context.recallId(documentKey(email, type)), decision, null);
  }

  @When("I approve the document with id {string}")
  public void iApproveTheDocumentWithId(String id) {
    review(UUID.fromString(id), "APPROVED", null);
  }

  @Then("the driver {string} should have status {string}")
  public void theDriverShouldHaveStatus(String email, String status) {
    User driver = userRepository.findByEmail(email).orElseThrow();
    assertThat(driver.getDriverStatus()).hasToString(status);
  }

  @Then("a {string} event should have been published")
  public void anEventShouldHaveBeenPublished(String eventName) {
    assertThat(recordedEvents.names()).contains(eventName);
  }

  @Then("no {string} event should have been published")
  public void noEventShouldHaveBeenPublished(String eventName) {
    assertThat(recordedEvents.names()).doesNotContain(eventName);
  }

  @Then("the uploaded file should be stored")
  public void theUploadedFileShouldBeStored() throws IOException {
    try (Stream<Path> files = Files.list(Path.of(storageProperties.path()))) {
      assertThat(files.toList()).hasSize(1);
    }
  }

  private void uploadAndRemember(String email, String type) {
    upload(type, type.toLowerCase() + ".pdf", "application/pdf", sampleContent("application/pdf"));
    assertThat(context.lastStatus()).as(context.lastBody()).isEqualTo(201);
    context.rememberId(documentKey(email, type), context.lastId());
  }

  private void upload(String type, String filename, String contentType, byte[] content) {
    MockMultipartFile file = new MockMultipartFile("file", filename, contentType, content);
    context.perform(multipart(DOCUMENTS_PATH).file(file).param("type", type));
  }

  private void review(UUID documentId, String decision, String reason) {
    Map<String, Object> body = new HashMap<>();
    body.put("decision", decision);
    body.put("reason", reason);
    context.patchJson("/api/v1/driver-documents/" + documentId + "/review", body);
  }

  private static byte[] sampleContent(String contentType) {
    byte[] header =
        switch (contentType) {
          case "image/jpeg" -> JPEG_HEADER;
          case "image/png" -> PNG_HEADER;
          case "application/pdf" -> PDF_HEADER;
          default -> "plain text content".getBytes(StandardCharsets.UTF_8);
        };
    byte[] content = new byte[header.length + 64];
    System.arraycopy(header, 0, content, 0, header.length);
    return content;
  }

  private static String documentKey(String email, String type) {
    return "document:" + email + ":" + type;
  }
}
