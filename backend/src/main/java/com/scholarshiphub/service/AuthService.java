package com.scholarshiphub.service;

import com.scholarshiphub.dto.request.RegisterRequest;
import com.scholarshiphub.dto.response.UserSummaryResponse;

public interface AuthService {

    UserSummaryResponse register(RegisterRequest request);

    AuthenticationResult login(String email, String password, String userAgent, String ipAddress);

    AuthenticationResult refresh(String rawRefreshToken, String userAgent, String ipAddress);

    void logout(String rawRefreshToken);

    void verifyEmail(String rawToken);

    void resendVerification(String email);

    void forgotPassword(String email);

    void resetPassword(String rawToken, String newPassword);

    void changePassword(Long userId, String currentPassword, String newPassword);
}
