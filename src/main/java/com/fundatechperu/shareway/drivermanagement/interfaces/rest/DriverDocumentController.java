package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.DocumentUpload;
import com.fundatechperu.shareway.drivermanagement.application.DriverDocumentService;
import com.fundatechperu.shareway.drivermanagement.application.DriverVerificationService;
import com.fundatechperu.shareway.drivermanagement.domain.model.DocumentType;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.DocumentResponse;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.ReviewDocumentRequest;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Tag(
    name = "Driver documents",
    description = "Upload of verification documents and their review by administrators")
public class DriverDocumentController {

  private final DriverDocumentService driverDocumentService;
  private final DriverVerificationService driverVerificationService;

  public DriverDocumentController(
      DriverDocumentService driverDocumentService,
      DriverVerificationService driverVerificationService) {
    this.driverDocumentService = driverDocumentService;
    this.driverVerificationService = driverVerificationService;
  }

  @PostMapping(path = "/drivers/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Upload a verification document",
      description = "Drivers only. Accepts PDF, JPEG or PNG files up to 5 MB.")
  public DocumentResponse upload(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @RequestParam("type") DocumentType type,
      @RequestPart("file") MultipartFile file)
      throws IOException {
    DocumentUpload upload =
        new DocumentUpload(
            type, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    return DocumentResponse.from(driverDocumentService.upload(principal.id(), upload));
  }

  @GetMapping("/drivers/me/documents")
  @Operation(summary = "List my documents", description = "Drivers only.")
  public List<DocumentResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return driverDocumentService.listForDriver(principal.id()).stream()
        .map(DocumentResponse::from)
        .toList();
  }

  @GetMapping("/drivers/me/documents/{id}")
  @Operation(
      summary = "Get one of my documents",
      description = "Drivers only. Returns 404 if the document is not mine.")
  public DocumentResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable UUID id) {
    return DocumentResponse.from(driverDocumentService.getForDriver(principal.id(), id));
  }

  @PatchMapping("/driver-documents/{id}/review")
  @Operation(
      summary = "Review a driver document",
      description =
          "Administrators only. A reason is required to reject. Approving DRIVERS_LICENSE, "
              + "NATIONAL_ID and CRIMINAL_RECORD verifies the driver.")
  public DocumentResponse review(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable UUID id,
      @Valid @RequestBody ReviewDocumentRequest request) {
    return DocumentResponse.from(
        driverVerificationService.review(id, principal.id(), request.decision(), request.reason()));
  }
}
