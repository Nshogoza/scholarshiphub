package com.scholarshiphub.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.scholarshiphub.dto.request.RequiredDocumentItem;
import com.scholarshiphub.dto.request.ScholarshipRequest;
import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.ScholarshipMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.ScholarshipRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.impl.ScholarshipServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Pure unit tests for scholarship lifecycle rules -- the deadline-in-the-
 * future guard and the DRAFT/PUBLISHED/CLOSED/ARCHIVED transition table --
 * with every collaborator mocked so no database or Spring context is needed.
 */
class ScholarshipServiceImplTest {

    @Mock private ScholarshipRepository scholarshipRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScholarshipMapper scholarshipMapper;
    @Mock private AuditLogService auditLogService;

    private ScholarshipServiceImpl scholarshipService;
    private User admin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scholarshipService = new ScholarshipServiceImpl(scholarshipRepository, applicationRepository, userRepository,
                scholarshipMapper, auditLogService);

        admin = User.builder().id(1L).email("admin@example.com").firstName("Ad").lastName("Min").build();
        when(scholarshipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_whenDeadlineNotInFuture_throwsInvalidState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        ScholarshipRequest request = requestWithDeadline(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> scholarshipService.create(request, 1L))
                .isInstanceOf(InvalidStateException.class);

        verify(scholarshipRepository, never()).save(any());
    }

    @Test
    void create_success_startsInDraftWithRequiredDocuments() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        ScholarshipRequest request = requestWithDeadline(Instant.now().plus(30, ChronoUnit.DAYS));

        scholarshipService.create(request, 1L);

        var captor = org.mockito.ArgumentCaptor.forClass(Scholarship.class);
        verify(scholarshipRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ScholarshipStatus.DRAFT);
        assertThat(captor.getValue().getRequiredDocuments()).hasSize(1);
        verify(auditLogService).record(eq(1L), eq("SCHOLARSHIP_CREATED"), eq("Scholarship"), any(), any());
    }

    @Test
    void create_whenAdminNotFound_throwsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ScholarshipRequest request = requestWithDeadline(Instant.now().plus(30, ChronoUnit.DAYS));

        assertThatThrownBy(() -> scholarshipService.create(request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenArchived_throwsInvalidState() {
        Scholarship archived = existingScholarship(ScholarshipStatus.ARCHIVED, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(archived));
        ScholarshipRequest request = requestWithDeadline(Instant.now().plus(60, ChronoUnit.DAYS));

        assertThatThrownBy(() -> scholarshipService.update(10L, request, 1L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void update_whenPublishedAndDeadlineNotFuture_throwsInvalidState() {
        Scholarship published = existingScholarship(ScholarshipStatus.PUBLISHED, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(published));
        ScholarshipRequest request = requestWithDeadline(Instant.now().minus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> scholarshipService.update(10L, request, 1L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void update_whenClosed_allowsPastDeadlineSinceNoLongerOpenForApplications() {
        Scholarship closed = existingScholarship(ScholarshipStatus.CLOSED, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(closed));
        ScholarshipRequest request = requestWithDeadline(Instant.now().minus(1, ChronoUnit.DAYS));

        scholarshipService.update(10L, request, 1L);

        assertThat(closed.getApplicationDeadline()).isBefore(Instant.now());
        verify(scholarshipRepository).save(closed);
    }

    @Test
    void updateStatus_illegalTransition_throwsInvalidState() {
        Scholarship draft = existingScholarship(ScholarshipStatus.DRAFT, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> scholarshipService.updateStatus(10L, ScholarshipStatus.CLOSED, 1L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void updateStatus_fromArchived_isAlwaysTerminal() {
        Scholarship archived = existingScholarship(ScholarshipStatus.ARCHIVED, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> scholarshipService.updateStatus(10L, ScholarshipStatus.PUBLISHED, 1L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void updateStatus_legalTransition_publishesTheDraft() {
        Scholarship draft = existingScholarship(ScholarshipStatus.DRAFT, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(draft));

        scholarshipService.updateStatus(10L, ScholarshipStatus.PUBLISHED, 1L);

        assertThat(draft.getStatus()).isEqualTo(ScholarshipStatus.PUBLISHED);
        verify(auditLogService).record(eq(1L), eq("SCHOLARSHIP_STATUS_CHANGED"), eq("Scholarship"), any(), any());
    }

    @Test
    void delete_whenExistingApplications_throwsInvalidState() {
        Scholarship scholarship = existingScholarship(ScholarshipStatus.PUBLISHED, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(scholarship));
        when(applicationRepository.countByScholarship_Id(10L)).thenReturn(3L);

        assertThatThrownBy(() -> scholarshipService.delete(10L, 1L))
                .isInstanceOf(InvalidStateException.class);

        verify(scholarshipRepository, never()).delete(any(Scholarship.class));
    }

    @Test
    void delete_whenNoApplications_deletesSuccessfully() {
        Scholarship scholarship = existingScholarship(ScholarshipStatus.DRAFT, Instant.now().plus(30, ChronoUnit.DAYS));
        when(scholarshipRepository.findById(10L)).thenReturn(Optional.of(scholarship));
        when(applicationRepository.countByScholarship_Id(10L)).thenReturn(0L);

        scholarshipService.delete(10L, 1L);

        verify(scholarshipRepository).delete(scholarship);
        verify(auditLogService).record(eq(1L), eq("SCHOLARSHIP_DELETED"), eq("Scholarship"), any(), any());
    }

    private Scholarship existingScholarship(ScholarshipStatus status, Instant deadline) {
        return Scholarship.builder()
                .id(10L)
                .title("Existing Scholarship")
                .description("Desc")
                .eligibilityCriteria("Any")
                .amount(BigDecimal.valueOf(500))
                .status(status)
                .applicationDeadline(deadline)
                .createdBy(admin)
                .build();
    }

    private ScholarshipRequest requestWithDeadline(Instant deadline) {
        return new ScholarshipRequest(
                "New Scholarship",
                "Description",
                "Eligibility",
                BigDecimal.valueOf(1000),
                deadline,
                List.of(new RequiredDocumentItem("Transcript", true)));
    }
}
