package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.ReviewRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.ApplicationDetailResponse;
import com.scholarshiphub.dto.response.ApplicationSummaryResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.dto.response.ReviewResponse;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.ApplicationService;
import com.scholarshiphub.service.ReviewService;
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
@RequestMapping("/api/v1/reviewer/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('REVIEWER')")
@Tag(name = "Reviewer Workflow", description = "Reviewing applications assigned to the authenticated reviewer")
public class ReviewController {

    private final ApplicationService applicationService;
    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "List applications assigned to the authenticated reviewer")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationSummaryResponse>>> listAssigned(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        var page = applicationService.listForReviewer(principal.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detail of an assigned application, including full review history")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        var detail = applicationService.getDetail(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "Record a review decision: approve, reject, or request additional information")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        var review = reviewService.addReview(id, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Review recorded", review));
    }
}
