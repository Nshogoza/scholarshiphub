package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.ApplicationCreateRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.ApplicationDetailResponse;
import com.scholarshiphub.dto.response.ApplicationDocumentResponse;
import com.scholarshiphub.dto.response.ApplicationSummaryResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.ApplicationService;
import com.scholarshiphub.service.DownloadableDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Student scholarship application workflow")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Start a new draft application for a scholarship")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApplicationCreateRequest request) {
        var created = applicationService.create(principal.getId(), request.scholarshipId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application draft created", created));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "List the authenticated student's own applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationSummaryResponse>>> listMine(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        var page = applicationService.listMine(principal.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application detail (owning student, assigned reviewer, or admin only)")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        var detail = applicationService.getDetail(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Upload a supporting document (PDF/DOCX, max 10MB) to a draft application")
    public ResponseEntity<ApiResponse<ApplicationDocumentResponse>> uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam String documentName,
            @RequestParam("file") MultipartFile file) {
        var document = applicationService.uploadDocument(id, principal.getId(), documentName, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Document uploaded", document));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Remove a document from a draft application")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long documentId) {
        applicationService.deleteDocument(id, documentId, principal.getId());
        return ResponseEntity.ok(ApiResponse.message("Document removed"));
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    @Operation(summary = "Download a document (owning student, assigned reviewer, or admin only)")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long documentId) {
        DownloadableDocument doc = applicationService.downloadDocument(
                id, documentId, principal.getId(), principal.getRole());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.filename() + "\"")
                .body(doc.resource());
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit a draft application for review (validates required documents and deadline)")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        var submitted = applicationService.submit(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Application submitted", submitted));
    }
}
