package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.CreateUserRequest;
import com.scholarshiphub.dto.request.UpdateUserStatusRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.UserManagementService;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management", description = "Administrator-only account management")
public class AdminUserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @Operation(summary = "List all users with optional role/status/email filters, paginated")
    public ResponseEntity<ApiResponse<PageResponse<UserSummaryResponse>>> listUsers(
            @RequestParam(required = false) RoleName role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = userManagementService.listUsers(role, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a single user's details")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userManagementService.getUser(userId)));
    }

    @PostMapping
    @Operation(summary = "Provision a REVIEWER or ADMIN account (or a STUDENT account on the admin's behalf)")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        var created = userManagementService.createUser(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created", created));
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Activate, lock, or disable a user account")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        var updated = userManagementService.updateStatus(userId, request.status(), principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("User status updated", updated));
    }
}
