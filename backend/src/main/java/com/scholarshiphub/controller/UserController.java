package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.UpdateProfileRequest;
import com.scholarshiphub.dto.request.UpdateStudentProfileRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.StudentProfileResponse;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Self-service profile management for the authenticated user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's account details")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUser(principal.getId())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the currently authenticated user's basic account details")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(principal.getId(), request)));
    }

    @GetMapping("/me/student-profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get the authenticated student's academic profile")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getStudentProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getStudentProfile(principal.getId())));
    }

    @PutMapping("/me/student-profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update the authenticated student's academic profile")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateStudentProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateStudentProfile(principal.getId(), request)));
    }
}
