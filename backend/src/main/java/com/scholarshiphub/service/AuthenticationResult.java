package com.scholarshiphub.service;

import com.scholarshiphub.dto.response.AuthResponse;

/**
 * Application-layer result of a login/refresh operation. Not a wire DTO --
 * the controller unpacks {@code rawRefreshToken} into an httpOnly cookie via
 * {@link com.scholarshiphub.security.CookieUtil} and returns only
 * {@code response} to the client.
 */
public record AuthenticationResult(
        AuthResponse response,
        String rawRefreshToken,
        long refreshTokenMaxAgeSeconds
) {
}
