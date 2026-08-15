package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.ScholarshipRequest;
import com.scholarshiphub.dto.request.ScholarshipStatusUpdateRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.dto.response.ScholarshipResponse;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.ScholarshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/scholarships")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Scholarships", description = "Full scholarship lifecycle management")
public class AdminScholarshipController {

    private final ScholarshipService scholarshipService;

    @GetMapping
    @Operation(summary = "List all scholarships regardless of status")
    public ResponseEntity<ApiResponse<PageResponse<ScholarshipResponse>>> list(
            @RequestParam(required = false) ScholarshipStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        var page = scholarshipService.adminList(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @PostMapping
    @Operation(summary = "Create a new scholarship (starts as DRAFT)")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScholarshipRequest request) {
        var created = scholarshipService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Scholarship created", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit scholarship details")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ScholarshipRequest request) {
        var updated = scholarshipService.update(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Scholarship updated", updated));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition scholarship status (DRAFT -> PUBLISHED -> CLOSED -> ARCHIVED)")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ScholarshipStatusUpdateRequest request) {
        var updated = scholarshipService.updateStatus(id, request.status(), principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Scholarship status updated", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a scholarship (only if it has no applications)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        scholarshipService.delete(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.message("Scholarship deleted"));
    }
}
