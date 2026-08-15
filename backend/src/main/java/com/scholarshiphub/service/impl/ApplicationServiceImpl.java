package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.response.ApplicationDetailResponse;
import com.scholarshiphub.dto.response.ApplicationDocumentResponse;
import com.scholarshiphub.dto.response.ApplicationSummaryResponse;
import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.ApplicationDocument;
import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.ScholarshipRequiredDocument;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import com.scholarshiphub.exception.DuplicateResourceException;
import com.scholarshiphub.exception.FileValidationException;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.ApplicationMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.ScholarshipRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.repository.specification.ApplicationSpecifications;
import com.scholarshiphub.service.ApplicationService;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.DownloadableDocument;
import com.scholarshiphub.service.MailService;
import com.scholarshiphub.service.StorageService;
import com.scholarshiphub.service.StoredFile;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ApplicationMapper applicationMapper;
    private final MailService mailService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ApplicationDetailResponse create(Long studentId, Long scholarshipId) {
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", scholarshipId));
        if (scholarship.getStatus() != ScholarshipStatus.PUBLISHED) {
            throw new InvalidStateException("This scholarship is not currently open for applications");
        }
        if (scholarship.isPastDeadline()) {
            throw new InvalidStateException("The application deadline for this scholarship has passed");
        }

        applicationRepository.findByScholarship_IdAndStudent_Id(scholarshipId, studentId).ifPresent(a -> {
            throw new DuplicateResourceException("You have already started or submitted an application for this scholarship");
        });

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        Application application = Application.builder()
                .scholarship(scholarship)
                .student(student)
                .status(ApplicationStatus.DRAFT)
                .build();
        application = applicationRepository.save(application);

        auditLogService.record(studentId, "APPLICATION_CREATED", "Application", application.getId(),
                "Draft application started for scholarship '" + scholarship.getTitle() + "'");

        return applicationMapper.toDetail(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> listMine(Long studentId, ApplicationStatus status, Pageable pageable) {
        Specification<Application> spec = Specification.where(ApplicationSpecifications.forStudent(studentId))
                .and(ApplicationSpecifications.hasStatus(status));
        return applicationRepository.findAll(spec, pageable).map(applicationMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDetailResponse getDetail(Long applicationId, Long requesterId, RoleName requesterRole) {
        Application application = findApplication(applicationId);
        assertCanView(application, requesterId, requesterRole);
        return applicationMapper.toDetail(application);
    }

    @Override
    @Transactional
    public ApplicationDocumentResponse uploadDocument(Long applicationId, Long studentId, String documentName,
                                                       MultipartFile file) {
        if (documentName == null || documentName.isBlank()) {
            throw new FileValidationException("Document name is required");
        }
        if (documentName.length() > 255) {
            throw new FileValidationException("Document name must not exceed 255 characters");
        }

        Application application = findApplication(applicationId);
        assertOwner(application, studentId);
        assertEditable(application);

        StoredFile stored = storageService.store(file, "applications/" + applicationId);

        ApplicationDocument document = ApplicationDocument.builder()
                .documentName(documentName)
                .originalFilename(stored.originalFilename())
                .storedPath(stored.storedPath())
                .contentType(stored.contentType())
                .fileSizeBytes(stored.sizeBytes())
                .checksumSha256(stored.checksumSha256())
                .build();
        application.addDocument(document);
        applicationRepository.save(application);

        auditLogService.record(studentId, "APPLICATION_DOCUMENT_UPLOADED", "Application", applicationId,
                "Uploaded document '" + documentName + "' (" + stored.originalFilename() + ")");

        return applicationMapper.toDocumentResponse(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long applicationId, Long documentId, Long studentId) {
        Application application = findApplication(applicationId);
        assertOwner(application, studentId);
        assertEditable(application);

        ApplicationDocument document = application.getDocuments().stream()
                .filter(d -> d.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationDocument", documentId));

        storageService.delete(document.getStoredPath());
        application.getDocuments().remove(document);
        applicationRepository.save(application);

        auditLogService.record(studentId, "APPLICATION_DOCUMENT_DELETED", "Application", applicationId,
                "Deleted document '" + document.getDocumentName() + "'");
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadableDocument downloadDocument(Long applicationId, Long documentId, Long requesterId,
                                                  RoleName requesterRole) {
        Application application = findApplication(applicationId);
        assertCanView(application, requesterId, requesterRole);

        ApplicationDocument document = application.getDocuments().stream()
                .filter(d -> d.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationDocument", documentId));

        return new DownloadableDocument(
                storageService.loadAsResource(document.getStoredPath()),
                document.getOriginalFilename(),
                document.getContentType());
    }

    @Override
    @Transactional
    public ApplicationDetailResponse submit(Long applicationId, Long studentId) {
        Application application = findApplication(applicationId);
        assertOwner(application, studentId);
        assertEditable(application);

        Scholarship scholarship = application.getScholarship();
        if (scholarship.isPastDeadline()) {
            throw new InvalidStateException("The application deadline for this scholarship has passed");
        }

        List<String> missing = missingMandatoryDocuments(application, scholarship);
        if (!missing.isEmpty()) {
            throw new InvalidStateException(
                    "The following required documents are missing: " + String.join(", ", missing));
        }

        boolean reviewerAlreadyAssigned = application.getReviewer() != null;
        application.setStatus(reviewerAlreadyAssigned ? ApplicationStatus.UNDER_REVIEW : ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(Instant.now());
        applicationRepository.save(application);

        mailService.sendApplicationStatusChangedEmail(
                application.getStudent(), scholarship.getTitle(), application.getStatus().name());
        auditLogService.record(studentId, "APPLICATION_SUBMITTED", "Application", applicationId,
                "Application submitted for scholarship '" + scholarship.getTitle() + "'");

        return applicationMapper.toDetail(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> listForReviewer(Long reviewerId, ApplicationStatus status,
                                                             Pageable pageable) {
        Specification<Application> spec = Specification.where(ApplicationSpecifications.forReviewer(reviewerId))
                .and(ApplicationSpecifications.hasStatus(status));
        return applicationRepository.findAll(spec, pageable).map(applicationMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> adminList(ApplicationStatus status, Long scholarshipId,
                                                       Pageable pageable) {
        Specification<Application> spec = Specification.where(ApplicationSpecifications.hasStatus(status))
                .and(ApplicationSpecifications.forScholarship(scholarshipId));
        return applicationRepository.findAll(spec, pageable).map(applicationMapper::toSummary);
    }

    @Override
    @Transactional
    public ApplicationSummaryResponse assignReviewer(Long applicationId, Long reviewerId, Long actorAdminId) {
        Application application = findApplication(applicationId);

        if (application.getStatus() != ApplicationStatus.SUBMITTED
                && application.getStatus() != ApplicationStatus.UNDER_REVIEW
                && application.getStatus() != ApplicationStatus.ADDITIONAL_INFO_REQUIRED) {
            throw new InvalidStateException(
                    "Cannot assign a reviewer to an application in status " + application.getStatus());
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewerId));
        if (reviewer.getRole().getName() != RoleName.REVIEWER) {
            throw new InvalidStateException("Selected user does not have the REVIEWER role");
        }

        application.setReviewer(reviewer);
        if (application.getStatus() == ApplicationStatus.SUBMITTED) {
            application.setStatus(ApplicationStatus.UNDER_REVIEW);
        }
        applicationRepository.save(application);

        mailService.sendReviewerAssignedEmail(
                reviewer, application.getScholarship().getTitle(), application.getStudent().getFullName());
        auditLogService.record(actorAdminId, "REVIEWER_ASSIGNED", "Application", applicationId,
                "Assigned reviewer " + reviewer.getEmail() + " to application " + applicationId);

        return applicationMapper.toSummary(application);
    }

    // ---- internal helpers -------------------------------------------------

    private Application findApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
    }

    private void assertOwner(Application application, Long studentId) {
        if (!application.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("You do not own this application");
        }
    }

    private void assertEditable(Application application) {
        if (!application.isEditableByStudent()) {
            throw new InvalidStateException(
                    "Application cannot be modified while in status " + application.getStatus());
        }
    }

    private void assertCanView(Application application, Long requesterId, RoleName requesterRole) {
        boolean isOwner = application.getStudent().getId().equals(requesterId);
        boolean isAssignedReviewer = application.getReviewer() != null
                && application.getReviewer().getId().equals(requesterId);
        boolean isAdmin = requesterRole == RoleName.ADMIN;

        if (!(isOwner || isAssignedReviewer || isAdmin)) {
            throw new AccessDeniedException("You do not have access to this application");
        }
    }

    private List<String> missingMandatoryDocuments(Application application, Scholarship scholarship) {
        var uploadedNames = application.getDocuments().stream()
                .map(d -> d.getDocumentName().trim().toLowerCase())
                .collect(Collectors.toSet());

        return scholarship.getRequiredDocuments().stream()
                .filter(ScholarshipRequiredDocument::isMandatory)
                .map(ScholarshipRequiredDocument::getDocumentName)
                .filter(name -> !uploadedNames.contains(name.trim().toLowerCase()))
                .toList();
    }
}
