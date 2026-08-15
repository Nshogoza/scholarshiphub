package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.request.ReviewRequest;
import com.scholarshiphub.dto.response.ReviewResponse;
import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.Review;
import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.exception.InvalidStateException;
import com.scholarshiphub.exception.ResourceNotFoundException;
import com.scholarshiphub.mapper.ApplicationMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.ReviewRepository;
import com.scholarshiphub.service.AuditLogService;
import com.scholarshiphub.service.MailService;
import com.scholarshiphub.service.ReviewService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final ApplicationMapper applicationMapper;
    private final MailService mailService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ReviewResponse addReview(Long applicationId, Long reviewerId, ReviewRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));

        User assignedReviewer = application.getReviewer();
        if (assignedReviewer == null || !assignedReviewer.getId().equals(reviewerId)) {
            throw new AccessDeniedException("You are not the reviewer assigned to this application");
        }
        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new InvalidStateException(
                    "Application must be UNDER_REVIEW to record a review (current status: "
                            + application.getStatus() + ")");
        }

        Review review = Review.builder()
                .application(application)
                .reviewer(assignedReviewer)
                .score(request.score())
                .comments(request.comments())
                .recommendation(request.recommendation())
                .createdAt(Instant.now())
                .build();
        review = reviewRepository.save(review);

        applyRecommendation(application, request);
        applicationRepository.save(application);

        mailService.sendApplicationStatusChangedEmail(
                application.getStudent(), application.getScholarship().getTitle(), application.getStatus().name());
        auditLogService.record(reviewerId, "REVIEW_SUBMITTED", "Application", applicationId,
                "Reviewer recorded recommendation " + request.recommendation()
                        + " -> application status " + application.getStatus());

        return applicationMapper.toReviewResponse(review);
    }

    private void applyRecommendation(Application application, ReviewRequest request) {
        switch (request.recommendation()) {
            case APPROVE -> {
                application.setStatus(ApplicationStatus.APPROVED);
                application.setDecidedAt(Instant.now());
            }
            case REJECT -> {
                application.setStatus(ApplicationStatus.REJECTED);
                application.setDecidedAt(Instant.now());
            }
            case REQUEST_ADDITIONAL_INFO -> application.setStatus(ApplicationStatus.ADDITIONAL_INFO_REQUIRED);
        }
    }
}
