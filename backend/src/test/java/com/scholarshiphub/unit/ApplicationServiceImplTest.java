package com.scholarshiphub.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.ScholarshipRequiredDocument;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import com.scholarshiphub.exception.DuplicateResourceException;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.ApplicationMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.ScholarshipRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.MailService;
import com.scholarshiphub.service.StorageService;
import com.scholarshiphub.service.impl.ApplicationServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

/**
 * Pure unit tests for the application lifecycle state machine -- deadlines,
 * duplicate submissions, editability, and reviewer assignment -- with every
 * collaborator mocked so no database or Spring context is required.
 */
class ApplicationServiceImplTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ScholarshipRepository scholarshipRepository;
    @Mock private UserRepository userRepository;
    @Mock private StorageService storageService;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private MailService mailService;
    @Mock private AuditLogService auditLogService;

    private ApplicationServiceImpl applicationService;

    private Scholarship scholarship;
    private User student;
    private User otherStudent;
    private User reviewer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new ApplicationServiceImpl(applicationRepository, scholarshipRepository, userRepository,
                storageService, applicationMapper, mailService, auditLogService);

        student = User.builder().id(1L).email("student@example.com").firstName("Stu").lastName("Dent").build();
        otherStudent = User.builder().id(2L).email("other@example.com").firstName("Other").lastName("Student").build();
        reviewer = User.builder().id(3L).email("reviewer@example.com").firstName("Rev").lastName("Iewer")
                .role(com.scholarshiphub.entity.Role.builder().id(2L).name(RoleName.REVIEWER).build())
                .build();

        scholarship = Scholarship.builder()
                .id(10L)
                .title("Merit Scholarship")
                .status(ScholarshipStatus.PUBLISHED)
                .amount(BigDecimal.valueOf(500))
                .applicationDeadline(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void create_whenScholarshipNotPublished_throwsInvalidState() {
        scholarship.setStatus(ScholarshipStatus.DRAFT);
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(scholarship));

        assertThatThrownBy(() -> applicationService.create(1L, 10L))
                .isInstanceOf(InvalidStateException.class);

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void create_whenPastDeadline_throwsInvalidState() {
        scholarship.setApplicationDeadline(Instant.now().minus(1, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(scholarship));

        assertThatThrownBy(() -> applicationService.create(1L, 10L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void create_whenScholarshipNotFound_throwsResourceNotFound() {
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.create(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenAlreadyApplied_throwsDuplicateResource() {
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(scholarship));
        when(applicationRepository.findByScholarship_IdAndStudent_Id(10L, 1L))
                .thenReturn(Optional.of(Application.builder().id(99L).build()));

        assertThatThrownBy(() -> applicationService.create(1L, 10L))
                .isInstanceOf(DuplicateResourceException.class);

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void create_success_savesDraftApplicationAndRecordsAudit() {
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(scholarship));
        when(applicationRepository.findByScholarship_IdAndStudent_Id(10L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        applicationService.create(1L, 10L);

        var captor = org.mockito.ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(captor.getValue().getStudent()).isEqualTo(student);
        verify(auditLogService).record(eq(1L), eq("APPLICATION_CREATED"), eq("Application"), any(), any());
    }

    @Test
    void submit_whenNotOwner_throwsAccessDenied() {
        Application application = draftApplication();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.submit(100L, otherStudent.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void submit_whenNotEditable_throwsInvalidState() {
        Application application = draftApplication();
        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.submit(100L, student.getId()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void submit_whenPastDeadline_throwsInvalidState() {
        scholarship.setApplicationDeadline(Instant.now().minus(1, ChronoUnit.DAYS));
        Application application = draftApplication();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.submit(100L, student.getId()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void submit_whenMandatoryDocumentMissing_throwsInvalidStateNamingIt() {
        scholarship.addRequiredDocument(ScholarshipRequiredDocument.builder()
                .documentName("Transcript").mandatory(true).build());
        Application application = draftApplication();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.submit(100L, student.getId()))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("Transcript");
    }

    @Test
    void submit_whenReviewerAlreadyAssigned_movesStraightToUnderReview() {
        Application application = draftApplication();
        application.setReviewer(reviewer);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.submit(100L, student.getId());

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(application.getSubmittedAt()).isNotNull();
        verify(mailService).sendApplicationStatusChangedEmail(eq(student), any(), eq("UNDER_REVIEW"));
    }

    @Test
    void submit_withoutReviewerAssigned_movesToSubmitted() {
        Application application = draftApplication();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.submit(100L, student.getId());

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }

    @Test
    void assignReviewer_whenApplicationInDraft_throwsInvalidState() {
        Application application = draftApplication();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.assignReviewer(100L, reviewer.getId(), 999L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void assignReviewer_whenUserIsNotAReviewer_throwsInvalidState() {
        Application application = draftApplication();
        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        student.setRole(com.scholarshiphub.entity.Role.builder().id(1L).name(RoleName.STUDENT).build());

        assertThatThrownBy(() -> applicationService.assignReviewer(100L, student.getId(), 999L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void assignReviewer_success_setsReviewerAndMovesToUnderReview() {
        Application application = draftApplication();
        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));

        applicationService.assignReviewer(100L, reviewer.getId(), 999L);

        assertThat(application.getReviewer()).isEqualTo(reviewer);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        verify(auditLogService).record(eq(999L), eq("REVIEWER_ASSIGNED"), eq("Application"), anyLong(), any());
    }

    private Application draftApplication() {
        return Application.builder()
                .id(100L)
                .scholarship(scholarship)
                .student(student)
                .status(ApplicationStatus.DRAFT)
                .build();
    }
}
