package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.AssignReviewerRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.ApplicationDetailResponse;
import com.scholarshiphub.dto.response.ApplicationSummaryResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Applications", description = "Platform-wide application oversight and reviewer assignment")
public class AdminApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "List all applications, optionally filtered by status and scholarship")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationSummaryResponse>>> list(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long scholarshipId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        var page = applicationService.adminList(status, scholarshipId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get any application's full detail")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        var detail = applicationService.getDetail(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @PatchMapping("/{id}/assign-reviewer")
    @Operation(summary = "Assign (or reassign) the reviewer responsible for an application")
    public ResponseEntity<ApiResponse<ApplicationSummaryResponse>> assignReviewer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AssignReviewerRequest request) {
        var updated = applicationService.assignReviewer(id, request.reviewerId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Reviewer assigned", updated));
    }
}
