package com.scholarshiphub.service;

import com.scholarshiphub.entity.User;

/** Outbound transactional email. Implementations must never throw upward
 *  and block a business operation on email delivery failure -- see
 *  {@link com.scholarshiphub.service.impl.MailServiceImpl} for the async/fail-soft contract. */
public interface MailService {

    void sendVerificationEmail(User user, String rawToken);

    void sendPasswordResetEmail(User user, String rawToken);

    void sendApplicationStatusChangedEmail(User student, String scholarshipTitle, String newStatus);

    void sendReviewerAssignedEmail(User reviewer, String scholarshipTitle, String studentName);
}
