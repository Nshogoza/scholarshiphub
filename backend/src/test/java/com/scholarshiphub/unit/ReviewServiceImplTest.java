package com.scholarshiphub.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.scholarshiphub.dto.request.ReviewRequest;
import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.entity.enums.ReviewRecommendation;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.mapper.ApplicationMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.ReviewRepository;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.MailService;
import com.scholarshiphub.service.impl.ReviewServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

/**
 * Pure unit tests for the review decision state machine -- assignment
 * ownership and the UNDER_REVIEW precondition guard the only path by which an
 * application can be approved, rejected, or bounced back to the student.
 */
class ReviewServiceImplTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private MailService mailService;
    @Mock private AuditLogService auditLogService;

    private ReviewServiceImpl reviewService;

    private User student;
    private User reviewer;
    private Application application;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewServiceImpl(applicationRepository, reviewRepository, applicationMapper,
                mailService, auditLogService);

        student = User.builder().id(1L).email("student@example.com").firstName("Stu").lastName("Dent").build();
        reviewer = User.builder().id(2L).email("reviewer@example.com").firstName("Rev").lastName("Iewer").build();

        Scholarship scholarship = Scholarship.builder().id(10L).title("Merit Scholarship").build();
        application = Application.builder()
                .id(100L)
                .scholarship(scholarship)
                .student(student)
                .reviewer(reviewer)
                .status(ApplicationStatus.UNDER_REVIEW)
                .build();

        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void addReview_whenNoReviewerAssigned_throwsAccessDenied() {
        application.setReviewer(null);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> reviewService.addReview(100L, reviewer.getId(), approveRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addReview_whenCallerIsNotTheAssignedReviewer_throwsAccessDenied() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> reviewService.addReview(100L, 999L, approveRequest()))
                .isInstanceOf(AccessDeniedException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_whenApplicationNotUnderReview_throwsInvalidState() {
        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> reviewService.addReview(100L, reviewer.getId(), approveRequest()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void addReview_withApprove_setsApprovedStatusAndDecidedAt() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        reviewService.addReview(100L, reviewer.getId(), approveRequest());

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(application.getDecidedAt()).isNotNull();
        verify(mailService).sendApplicationStatusChangedEmail(student, "Merit Scholarship", "APPROVED");
    }

    @Test
    void addReview_withReject_setsRejectedStatusAndDecidedAt() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        ReviewRequest request = new ReviewRequest(BigDecimal.valueOf(20), "Not eligible", ReviewRecommendation.REJECT);

        reviewService.addReview(100L, reviewer.getId(), request);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(application.getDecidedAt()).isNotNull();
    }

    @Test
    void addReview_withRequestAdditionalInfo_reopensToStudentWithoutDeciding() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        ReviewRequest request = new ReviewRequest(
                null, "Please upload transcript", ReviewRecommendation.REQUEST_ADDITIONAL_INFO);

        reviewService.addReview(100L, reviewer.getId(), request);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ADDITIONAL_INFO_REQUIRED);
        assertThat(application.getDecidedAt()).isNull();
    }

    private ReviewRequest approveRequest() {
        return new ReviewRequest(BigDecimal.valueOf(92), "Strong candidate", ReviewRecommendation.APPROVE);
    }
}
