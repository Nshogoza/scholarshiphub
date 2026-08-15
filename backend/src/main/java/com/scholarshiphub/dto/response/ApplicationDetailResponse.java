package com.scholarshiphub.dto.response;

import com.scholarshiphub.entity.enums.ApplicationStatus;
import java.time.Instant;
import java.util.List;

public record ApplicationDetailResponse(
        Long id,
        ScholarshipResponse scholarship,
        Long studentId,
        String studentName,
        Long reviewerId,
        String reviewerName,
        ApplicationStatus status,
        Instant submittedAt,
        Instant decidedAt,
        List<ApplicationDocumentResponse> documents,
        List<ReviewResponse> reviews,
        Instant createdAt,
        Instant updatedAt
) {
}
