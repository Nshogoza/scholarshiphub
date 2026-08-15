package com.scholarshiphub.dto.response;

/** Refresh token is deliberately absent -- it travels only as an httpOnly cookie. */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserSummaryResponse user
) {
    public static AuthResponse of(String accessToken, long expiresInSeconds, UserSummaryResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, user);
    }
}
