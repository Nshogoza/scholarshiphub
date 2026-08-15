package com.scholarshiphub.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.scholarshiphub.config.AppProperties;
import com.scholarshiphub.entity.Role;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import com.scholarshiphub.exception.AccountLockedException;
import com.scholarshiphub.exception.InvalidCredentialsException;
import com.scholarshiphub.mapper.UserMapper;
import com.scholarshiphub.repository.EmailVerificationTokenRepository;
import com.scholarshiphub.repository.PasswordResetTokenRepository;
import com.scholarshiphub.repository.RefreshTokenRepository;
import com.scholarshiphub.repository.RoleRepository;
import com.scholarshiphub.repository.StudentProfileRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.security.JwtTokenProvider;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.MailService;
import com.scholarshiphub.service.impl.AuthServiceImpl;
import com.scholarshiphub.util.SecureTokenGenerator;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pure unit tests for the login/lockout state machine -- the highest-risk
 * logic in the auth flow -- with every collaborator mocked so the test runs
 * in milliseconds with no database or Spring context.
 */
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private SecureTokenGenerator tokenGenerator;
    @Mock private UserMapper userMapper;
    @Mock private MailService mailService;
    @Mock private AuditLogService auditLogService;

    private AuthServiceImpl authService;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AppProperties appProperties = new AppProperties(
                new AppProperties.Frontend("http://localhost:5173"),
                new AppProperties.Cors("http://localhost:5173"),
                new AppProperties.Jwt("test-secret-test-secret-test-secret-1234", 15, 7, "scholarshiphub"),
                new AppProperties.Security(
                        new AppProperties.Security.Lockout(3, 15),
                        new AppProperties.Security.EmailVerification(24, true),
                        new AppProperties.Security.PasswordReset(30)),
                new AppProperties.Upload("./uploads", 10485760, "application/pdf"),
                new AppProperties.Cookies(false, ""));

        authService = new AuthServiceImpl(userRepository, roleRepository, studentProfileRepository,
                refreshTokenRepository, emailVerificationTokenRepository, passwordResetTokenRepository,
                passwordEncoder, jwtTokenProvider, tokenGenerator, userMapper, mailService, auditLogService,
                appProperties);

        Role studentRole = Role.builder().id(1L).name(RoleName.STUDENT).description("Student").build();
        user = User.builder()
                .id(1L)
                .email("student@example.com")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("Student")
                .role(studentRole)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void login_withWrongPassword_incrementsFailedAttemptsAndThrows() {
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login("student@example.com", "wrong", "agent", "1.2.3.4"))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void login_afterMaxFailedAttempts_locksAccount() {
        user.setFailedLoginAttempts(2); // one more failure hits the max of 3
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login("student@example.com", "wrong", "agent", "1.2.3.4"))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getLockedUntil()).isAfter(Instant.now());
    }

    @Test
    void login_whileLocked_throwsAccountLockedWithoutCheckingPassword() {
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(Instant.now().plusSeconds(600));
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("student@example.com", "irrelevant", "agent", "1.2.3.4"))
                .isInstanceOf(AccountLockedException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentialsWithoutRevealingExistence() {
        when(userRepository.findByEmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost@example.com", "whatever", "agent", "1.2.3.4"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
