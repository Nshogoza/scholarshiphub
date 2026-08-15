package com.scholarshiphub.controller;

import com.scholarshiphub.dto.request.ChangePasswordRequest;
import com.scholarshiphub.dto.request.ForgotPasswordRequest;
import com.scholarshiphub.dto.request.LoginRequest;
import com.scholarshiphub.dto.request.RegisterRequest;
import com.scholarshiphub.dto.request.ResendVerificationRequest;
import com.scholarshiphub.dto.request.ResetPasswordRequest;
import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.AuthResponse;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.exception.InvalidTokenException;
import com.scholarshiphub.security.CookieUtil;
import com.scholarshiphub.security.UserPrincipal;
import com.scholarshiphub.service.AuthService;
import com.scholarshiphub.service.AuthenticationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, tokens, and password/email flows")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserSummaryResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Registration successful. Please check your email to verify your account.", created));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive an access token; refresh token is set as an httpOnly cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse httpResponse) {
        AuthenticationResult result = authService.login(
                request.email(), request.password(), httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        cookieUtil.addRefreshTokenCookie(httpResponse, result.rawRefreshToken(), result.refreshTokenMaxAgeSeconds());
        return ResponseEntity.ok(ApiResponse.ok(result.response()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh token cookie and issue a new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest httpRequest,
                                                              HttpServletResponse httpResponse) {
        String rawRefreshToken = cookieUtil.readRefreshToken(httpRequest)
                .orElseThrow(() -> new InvalidTokenException("No refresh token present"));
        AuthenticationResult result = authService.refresh(
                rawRefreshToken, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        cookieUtil.addRefreshTokenCookie(httpResponse, result.rawRefreshToken(), result.refreshTokenMaxAgeSeconds());
        return ResponseEntity.ok(ApiResponse.ok(result.response()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the current refresh token session and clear the cookie")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        cookieUtil.readRefreshToken(httpRequest).ifPresent(authService::logout);
        cookieUtil.clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.ok(ApiResponse.message("Logged out successfully"));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify an email address using the token emailed to the user")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.message("Email verified successfully. You can now log in."));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend the email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
        return ResponseEntity.ok(ApiResponse.message(
                "If an account exists for that email, a verification link has been sent."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(ApiResponse.message(
                "If an account exists for that email, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset the password using a token emailed to the user")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message("Password reset successfully. Please log in again."));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password while authenticated")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message("Password changed successfully."));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
