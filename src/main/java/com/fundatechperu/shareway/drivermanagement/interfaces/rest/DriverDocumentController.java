package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.DocumentUpload;
import com.fundatechperu.shareway.drivermanagement.application.DriverDocumentService;
import com.fundatechperu.shareway.drivermanagement.application.DriverVerificationService;
import com.fundatechperu.shareway.drivermanagement.domain.model.DocumentType;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.DocumentResponse;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.ReviewDocumentRequest;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
      description =
          "Drivers only. Multipart form with the document type and the file: PDF, JPEG or PNG up"
              + " to 5 MB, checked by content type and by the first bytes of the file. The"
              + " document starts PENDING until an administrator reviews it.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Document stored and pending review",
        content =
            @Content(
                schema = @Schema(implementation = DocumentResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.DOCUMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Unknown type, missing or empty file, unsupported format or larger than 5 MB",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public DocumentResponse upload(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Parameter(description = "Type of the document", example = "DRIVERS_LICENSE")
          @RequestParam("type")
          DocumentType type,
      @Parameter(description = "PDF, JPEG or PNG file up to 5 MB") @RequestPart("file")
          MultipartFile file)
      throws IOException {
    DocumentUpload upload =
        new DocumentUpload(
            type, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    return DocumentResponse.from(driverDocumentService.upload(principal.id(), upload));
  }

  @GetMapping("/drivers/me/documents")
  @Operation(
      summary = "List my documents",
      description = "Drivers only. Every document uploaded by the driver, oldest first.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Documents of the driver",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = DocumentResponse.class)),
                examples =
                    @ExampleObject(value = DriverManagementExamples.DOCUMENT_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public List<DocumentResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return driverDocumentService.listForDriver(principal.id()).stream()
        .map(DocumentResponse::from)
        .toList();
  }

  @GetMapping("/drivers/me/documents/{id}")
  @Operation(
      summary = "Get one of my documents",
      description = "Drivers only. A document of another driver is reported as not found.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Document found",
        content =
            @Content(
                schema = @Schema(implementation = DocumentResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.DOCUMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a UUID",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown document, or it belongs to another driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public DocumentResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Parameter(description = "Document id", example = DriverManagementExamples.DOCUMENT_ID)
          @PathVariable
          UUID id) {
    return DocumentResponse.from(driverDocumentService.getForDriver(principal.id(), id));
  }

  @PatchMapping("/driver-documents/{id}/review")
  @Operation(
      summary = "Review a driver document",
      description =
          "Administrators only. APPROVED or REJECTED, with a reason required to reject. A document"
              + " is reviewed once. Approving DRIVERS_LICENSE, NATIONAL_ID and CRIMINAL_RECORD"
              + " verifies the driver and publishes DriverVerified; a rejection publishes"
              + " DocumentRejected.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples = {
                        @ExampleObject(
                            name = "reject",
                            value = DriverManagementExamples.REVIEW_REQUEST),
                        @ExampleObject(name = "approve", value = "{\"decision\": \"APPROVED\"}")
                      })))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Document reviewed",
        content =
            @Content(
                schema = @Schema(implementation = DocumentResponse.class),
                examples =
                    @ExampleObject(value = DriverManagementExamples.REVIEWED_DOCUMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Unknown decision, or a rejection without reason",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not an administrator",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown document",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "409",
        description = "The document was already reviewed",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(value = DriverManagementExamples.DOCUMENT_ALREADY_REVIEWED)))
  })
  public DocumentResponse review(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Parameter(description = "Document id", example = DriverManagementExamples.DOCUMENT_ID)
          @PathVariable
          UUID id,
      @Valid @RequestBody ReviewDocumentRequest request) {
    return DocumentResponse.from(
        driverVerificationService.review(id, principal.id(), request.decision(), request.reason()));
  }
}
