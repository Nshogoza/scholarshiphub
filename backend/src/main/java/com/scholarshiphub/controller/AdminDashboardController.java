package com.scholarshiphub.controller;

import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.AuditLogResponse;
import com.scholarshiphub.dto.response.DashboardAnalyticsResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.service.AnalyticsService;
import com.scholarshiphub.service.AuditLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Dashboard", description = "Platform analytics and audit trail")
public class AdminDashboardController {

    private final AnalyticsService analyticsService;
    private final AuditLogQueryService auditLogQueryService;

    @GetMapping("/analytics")
    @Operation(summary = "Totals, approval rate, and recent activity for the admin dashboard")
    public ResponseEntity<ApiResponse<DashboardAnalyticsResponse>> analytics() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDashboardAnalytics()));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Search the audit trail, optionally filtered by action or actor")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> auditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long actorUserId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        var page = auditLogQueryService.search(action, actorUserId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }
}
