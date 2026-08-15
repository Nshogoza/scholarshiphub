package com.scholarshiphub.service.impl;

import com.scholarshiphub.config.AppProperties;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends email asynchronously (@Async) so a slow/unreachable SMTP server
 * never blocks the HTTP request thread for register/login/apply/review
 * flows. Failures are logged, not propagated -- losing a notification email
 * must never fail the underlying business operation it describes.
 */
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private static final String FROM_ADDRESS = "no-reply@scholarshiphub.com";

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    @Async
    public void sendVerificationEmail(User user, String rawToken) {
        String link = appProperties.frontend().url() + "/verify-email?token=" + rawToken;
        String html = """
                <p>Hi %s,</p>
                <p>Welcome to ScholarshipHub. Please verify your email address to activate your account:</p>
                <p><a href="%s">Verify my email</a></p>
                <p>This link expires in %d hours. If you did not create this account, you can ignore this email.</p>
                """.formatted(user.getFirstName(), link, appProperties.security().emailVerification().tokenExpirationHours());
        send(user.getEmail(), "Verify your ScholarshipHub email", html);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String rawToken) {
        String link = appProperties.frontend().url() + "/reset-password?token=" + rawToken;
        String html = """
                <p>Hi %s,</p>
                <p>We received a request to reset your ScholarshipHub password.</p>
                <p><a href="%s">Reset my password</a></p>
                <p>This link expires in %d minutes. If you did not request this, you can safely ignore this email.</p>
                """.formatted(user.getFirstName(), link, appProperties.security().passwordReset().tokenExpirationMinutes());
        send(user.getEmail(), "Reset your ScholarshipHub password", html);
    }

    @Override
    @Async
    public void sendApplicationStatusChangedEmail(User student, String scholarshipTitle, String newStatus) {
        String html = """
                <p>Hi %s,</p>
                <p>The status of your application for <strong>%s</strong> has changed to
                <strong>%s</strong>.</p>
                <p>Log in to ScholarshipHub to view the details.</p>
                """.formatted(student.getFirstName(), scholarshipTitle, newStatus);
        send(student.getEmail(), "Update on your scholarship application", html);
    }

    @Override
    @Async
    public void sendReviewerAssignedEmail(User reviewer, String scholarshipTitle, String studentName) {
        String html = """
                <p>Hi %s,</p>
                <p>You have been assigned to review %s's application for <strong>%s</strong>.</p>
                <p>Log in to ScholarshipHub to begin your review.</p>
                """.formatted(reviewer.getFirstName(), studentName, scholarshipTitle);
        send(reviewer.getEmail(), "New application assigned for review", html);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send email to {} with subject '{}': {}", to, subject, ex.getMessage());
        }
    }
}
