package com.scholarshiphub.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding of the {@code app.*} configuration tree (see
 * application.yml). Registered automatically via
 * {@code @ConfigurationPropertiesScan} on the main application class.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Frontend frontend,
        Cors cors,
        Jwt jwt,
        Security security,
        Upload upload,
        Cookies cookies
) {

    public record Frontend(String url) {
    }

    public record Cors(String allowedOrigins) {
        public List<String> allowedOriginsList() {
            return Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    public record Jwt(String secret, int accessTokenExpirationMinutes,
                       int refreshTokenExpirationDays, String issuer) {
    }

    public record Security(Lockout lockout, EmailVerification emailVerification, PasswordReset passwordReset) {

        public record Lockout(int maxAttempts, int durationMinutes) {
        }

        public record EmailVerification(int tokenExpirationHours, boolean requireVerifiedEmailToLogin) {
        }

        public record PasswordReset(int tokenExpirationMinutes) {
        }
    }

    public record Upload(String directory, long maxFileSizeBytes, String allowedContentTypes) {
        public List<String> allowedContentTypesList() {
            return Arrays.stream(allowedContentTypes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    public record Cookies(boolean secure, String domain) {
    }
}
