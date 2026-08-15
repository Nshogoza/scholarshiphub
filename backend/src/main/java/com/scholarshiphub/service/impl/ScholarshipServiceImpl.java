package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.request.RequiredDocumentItem;
import com.scholarshiphub.dto.request.ScholarshipRequest;
import com.scholarshiphub.dto.response.ScholarshipResponse;
import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.ScholarshipRequiredDocument;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.ScholarshipMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.ScholarshipRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.repository.specification.ScholarshipSpecifications;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.ScholarshipService;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScholarshipServiceImpl implements ScholarshipService {

    /** Legal scholarship lifecycle transitions; ARCHIVED is terminal. */
    private static final Map<ScholarshipStatus, Set<ScholarshipStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ScholarshipStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ScholarshipStatus.DRAFT, EnumSet.of(ScholarshipStatus.PUBLISHED));
        ALLOWED_TRANSITIONS.put(ScholarshipStatus.PUBLISHED,
                EnumSet.of(ScholarshipStatus.CLOSED, ScholarshipStatus.ARCHIVED));
        ALLOWED_TRANSITIONS.put(ScholarshipStatus.CLOSED,
                EnumSet.of(ScholarshipStatus.PUBLISHED, ScholarshipStatus.ARCHIVED));
        ALLOWED_TRANSITIONS.put(ScholarshipStatus.ARCHIVED, EnumSet.noneOf(ScholarshipStatus.class));
    }

    private final ScholarshipRepository scholarshipRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ScholarshipMapper scholarshipMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public Page<ScholarshipResponse> browsePublished(String search, Pageable pageable) {
        Specification<Scholarship> spec = Specification
                .where(ScholarshipSpecifications.hasStatus(ScholarshipStatus.PUBLISHED))
                .and(ScholarshipSpecifications.titleContains(search));
        return scholarshipRepository.findAll(spec, pageable).map(scholarshipMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScholarshipResponse> adminList(ScholarshipStatus status, String search, Pageable pageable) {
        Specification<Scholarship> spec = Specification.where(ScholarshipSpecifications.hasStatus(status))
                .and(ScholarshipSpecifications.titleContains(search));
        return scholarshipRepository.findAll(spec, pageable).map(scholarshipMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ScholarshipResponse getById(Long id) {
        return scholarshipMapper.toResponse(findScholarship(id));
    }

    @Override
    @Transactional
    public ScholarshipResponse create(ScholarshipRequest request, Long actorAdminId) {
        User admin = userRepository.findById(actorAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorAdminId));

        if (!request.applicationDeadline().isAfter(Instant.now())) {
            throw new InvalidStateException("Application deadline must be in the future");
        }

        Scholarship scholarship = Scholarship.builder()
                .title(request.title())
                .description(request.description())
                .eligibilityCriteria(request.eligibilityCriteria())
                .amount(request.amount())
                .applicationDeadline(request.applicationDeadline())
                .status(ScholarshipStatus.DRAFT)
                .createdBy(admin)
                .build();
        applyRequiredDocuments(scholarship, request.requiredDocuments());

        scholarship = scholarshipRepository.save(scholarship);
        auditLogService.record(actorAdminId, "SCHOLARSHIP_CREATED", "Scholarship", scholarship.getId(),
                "Created scholarship '" + scholarship.getTitle() + "' (DRAFT)");

        return scholarshipMapper.toResponse(scholarship);
    }

    @Override
    @Transactional
    public ScholarshipResponse update(Long id, ScholarshipRequest request, Long actorAdminId) {
        Scholarship scholarship = findScholarship(id);
        if (scholarship.getStatus() == ScholarshipStatus.ARCHIVED) {
            throw new InvalidStateException("Archived scholarships cannot be edited");
        }
        boolean stillOpenForApplications = scholarship.getStatus() == ScholarshipStatus.DRAFT
                || scholarship.getStatus() == ScholarshipStatus.PUBLISHED;
        if (stillOpenForApplications && !request.applicationDeadline().isAfter(Instant.now())) {
            throw new InvalidStateException(
                    "Application deadline must be in the future while the scholarship is open");
        }

        scholarship.setTitle(request.title());
        scholarship.setDescription(request.description());
        scholarship.setEligibilityCriteria(request.eligibilityCriteria());
        scholarship.setAmount(request.amount());
        scholarship.setApplicationDeadline(request.applicationDeadline());

        scholarship.getRequiredDocuments().clear();
        applyRequiredDocuments(scholarship, request.requiredDocuments());

        scholarship = scholarshipRepository.save(scholarship);
        auditLogService.record(actorAdminId, "SCHOLARSHIP_UPDATED", "Scholarship", scholarship.getId(),
                "Updated scholarship '" + scholarship.getTitle() + "'");

        return scholarshipMapper.toResponse(scholarship);
    }

    @Override
    @Transactional
    public ScholarshipResponse updateStatus(Long id, ScholarshipStatus newStatus, Long actorAdminId) {
        Scholarship scholarship = findScholarship(id);
        ScholarshipStatus current = scholarship.getStatus();

        if (!ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(ScholarshipStatus.class))
                .contains(newStatus)) {
            throw new InvalidStateException(
                    "Cannot transition scholarship from " + current + " to " + newStatus);
        }

        scholarship.setStatus(newStatus);
        scholarshipRepository.save(scholarship);
        auditLogService.record(actorAdminId, "SCHOLARSHIP_STATUS_CHANGED", "Scholarship", scholarship.getId(),
                "Status changed from " + current + " to " + newStatus);

        return scholarshipMapper.toResponse(scholarship);
    }

    @Override
    @Transactional
    public void delete(Long id, Long actorAdminId) {
        Scholarship scholarship = findScholarship(id);
        if (applicationRepository.countByScholarship_Id(id) > 0) {
            throw new InvalidStateException(
                    "Scholarship has existing applications and cannot be deleted; archive it instead");
        }
        scholarshipRepository.delete(scholarship);
        auditLogService.record(actorAdminId, "SCHOLARSHIP_DELETED", "Scholarship", id,
                "Deleted scholarship '" + scholarship.getTitle() + "'");
    }

    private void applyRequiredDocuments(Scholarship scholarship, Iterable<RequiredDocumentItem> items) {
        for (RequiredDocumentItem item : items) {
            scholarship.addRequiredDocument(ScholarshipRequiredDocument.builder()
                    .documentName(item.documentName())
                    .mandatory(item.mandatory())
                    .build());
        }
    }

    private Scholarship findScholarship(Long id) {
        return scholarshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scholarship", id));
    }
}
