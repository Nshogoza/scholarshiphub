package com.scholarshiphub.security;

import com.scholarshiphub.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Creates/reads/clears the httpOnly refresh-token cookie. */
@Component
@RequiredArgsConstructor
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final AppProperties appProperties;

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(appProperties.cookies().secure())
                .path(COOKIE_PATH)
                .sameSite("Strict")
                .maxAge(maxAgeSeconds);
        if (appProperties.cookies().domain() != null && !appProperties.cookies().domain().isBlank()) {
            builder.domain(appProperties.cookies().domain());
        }
        response.addHeader("Set-Cookie", builder.build().toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        addRefreshTokenCookie(response, "", 0);
    }

    public Optional<String> readRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> !v.isBlank())
                .findFirst();
    }
}
