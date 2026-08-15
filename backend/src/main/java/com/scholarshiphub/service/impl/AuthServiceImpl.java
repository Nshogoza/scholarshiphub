package com.scholarshiphub.service.impl;

import com.scholarshiphub.config.AppProperties;
import com.scholarshiphub.dto.request.RegisterRequest;
import com.scholarshiphub.dto.response.AuthResponse;
import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.entity.EmailVerificationToken;
import com.scholarshiphub.entity.PasswordResetToken;
import com.scholarshiphub.entity.RefreshToken;
import com.scholarshiphub.entity.Role;
import com.scholarshiphub.entity.StudentProfile;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import com.scholarshiphub.exception.AccountLockedException;
import com.scholarshiphub.exception.DuplicateResourceException;
import com.scholarshiphub.exception.EmailNotVerifiedException;
import com.scholarshiphub.exception.InvalidCredentialsException;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.exception.InvalidTokenException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.UserMapper;
import com.scholarshiphub.repository.EmailVerificationTokenRepository;
import com.scholarshiphub.repository.PasswordResetTokenRepository;
import com.scholarshiphub.repository.RefreshTokenRepository;
import com.scholarshiphub.repository.RoleRepository;
import com.scholarshiphub.repository.StudentProfileRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.security.JwtTokenProvider;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.AuthService;
import com.scholarshiphub.service.AuthenticationResult;
import com.scholarshiphub.service.MailService;
import com.scholarshiphub.util.SecureTokenGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureTokenGenerator tokenGenerator;
    private final UserMapper userMapper;
    private final MailService mailService;
    private final AuditLogService auditLogService;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public UserSummaryResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role missing -- check Flyway seed data"));

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(studentRole)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);

        studentProfileRepository.save(StudentProfile.builder().user(user).build());

        issueEmailVerificationToken(user);
        auditLogService.record(user.getId(), "USER_REGISTERED", "User", user.getId(),
                "New student account registered: " + user.getEmail());

        return userMapper.toSummary(user);
    }

    @Override
    @Transactional
    public AuthenticationResult login(String email, String password, String userAgent, String ipAddress) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        enforceNotLocked(user);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new InvalidCredentialsException();
        }

        if (appProperties.security().emailVerification().requireVerifiedEmailToLogin() && !user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        auditLogService.record(user.getId(), "LOGIN_SUCCESS", "User", user.getId(),
                "Successful login for " + user.getEmail());

        return issueTokens(user, UUID.randomUUID(), userAgent, ipAddress);
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        String hash = tokenGenerator.sha256(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (existing.getRevokedAt() != null) {
            // A previously-rotated-out token was presented again: possible theft.
            // Kill the entire chain so a stolen token cannot be used further.
            refreshTokenRepository.revokeFamily(existing.getFamilyId(), Instant.now());
            auditLogService.record(existing.getUser().getId(), "REFRESH_TOKEN_REUSE_DETECTED", "User",
                    existing.getUser().getId(), "Revoked refresh token was reused; entire session family revoked");
            throw new InvalidTokenException("Refresh token has already been used; all sessions were revoked");
        }

        if (!existing.isActive()) {
            throw InvalidTokenException.expired();
        }

        User user = existing.getUser();
        enforceNotLocked(user);

        AuthenticationResult result = issueTokens(user, existing.getFamilyId(), userAgent, ipAddress);

        existing.setRevokedAt(Instant.now());
        existing.setReplacedByTokenHash(tokenGenerator.sha256(result.rawRefreshToken()));
        refreshTokenRepository.save(existing);

        return result;
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String hash = tokenGenerator.sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            refreshTokenRepository.revokeFamily(token.getFamilyId(), Instant.now());
            auditLogService.record(token.getUser().getId(), "LOGOUT", "User", token.getUser().getId(),
                    "User logged out; session family revoked");
        });
    }

    @Override
    @Transactional
    public void verifyEmail(String rawToken) {
        String hash = tokenGenerator.sha256(rawToken);
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidTokenException::expired);

        if (!token.isValid()) {
            throw InvalidTokenException.expired();
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(token);

        auditLogService.record(user.getId(), "EMAIL_VERIFIED", "User", user.getId(),
                "Email address verified for " + user.getEmail());
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (user.isEmailVerified()) {
                throw new InvalidStateException("This email address is already verified");
            }
            issueEmailVerificationToken(user);
        });
        // Silent no-op when the email is unknown -- prevents account enumeration.
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String rawToken = tokenGenerator.generate();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenGenerator.sha256(rawToken))
                    .expiresAt(Instant.now().plus(
                            appProperties.security().passwordReset().tokenExpirationMinutes(), ChronoUnit.MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            mailService.sendPasswordResetEmail(user, rawToken);
            auditLogService.record(user.getId(), "PASSWORD_RESET_REQUESTED", "User", user.getId(),
                    "Password reset requested for " + user.getEmail());
        });
        // Silent no-op when the email is unknown -- prevents account enumeration.
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String hash = tokenGenerator.sha256(rawToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidTokenException::expired);

        if (!token.isValid()) {
            throw InvalidTokenException.expired();
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        refreshTokenRepository.revokeAllForUser(user.getId(), Instant.now());
        auditLogService.record(user.getId(), "PASSWORD_RESET_COMPLETED", "User", user.getId(),
                "Password reset completed; all sessions revoked for " + user.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        refreshTokenRepository.revokeAllForUser(user.getId(), Instant.now());
        auditLogService.record(user.getId(), "PASSWORD_CHANGED", "User", user.getId(),
                "Password changed by account owner; all other sessions revoked");
    }

    // ---- internal helpers -------------------------------------------------

    private void enforceNotLocked(User user) {
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
                throw new AccountLockedException(user.getLockedUntil());
            }
            // Lockout window has elapsed -- auto-unlock.
            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    private void registerFailedAttempt(User user) {
        int maxAttempts = appProperties.security().lockout().maxAttempts();
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxAttempts) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(Instant.now().plus(
                    appProperties.security().lockout().durationMinutes(), ChronoUnit.MINUTES));
            auditLogService.record(user.getId(), "ACCOUNT_LOCKED", "User", user.getId(),
                    "Account locked after " + attempts + " failed login attempts");
        } else {
            auditLogService.record(user.getId(), "LOGIN_FAILED", "User", user.getId(),
                    "Failed login attempt " + attempts + "/" + maxAttempts);
        }
        userRepository.save(user);
    }

    private void issueEmailVerificationToken(User user) {
        String rawToken = tokenGenerator.generate();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenGenerator.sha256(rawToken))
                .expiresAt(Instant.now().plus(
                        appProperties.security().emailVerification().tokenExpirationHours(), ChronoUnit.HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        mailService.sendVerificationEmail(user, rawToken);
    }

    private AuthenticationResult issueTokens(User user, UUID familyId, String userAgent, String ipAddress) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().getName().name());

        String rawRefreshToken = tokenGenerator.generate();
        long refreshDays = appProperties.jwt().refreshTokenExpirationDays();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenGenerator.sha256(rawRefreshToken))
                .familyId(familyId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.save(refreshToken);

        long accessExpirySeconds = appProperties.jwt().accessTokenExpirationMinutes() * 60L;
        AuthResponse response = AuthResponse.of(accessToken, accessExpirySeconds, userMapper.toSummary(user));

        return new AuthenticationResult(response, rawRefreshToken, refreshDays * 24 * 60 * 60);
    }
}
