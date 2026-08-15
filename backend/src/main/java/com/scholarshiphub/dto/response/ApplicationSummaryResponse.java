package com.scholarshiphub.dto.response;

import com.scholarshiphub.entity.enums.ApplicationStatus;
import java.time.Instant;

public record ApplicationSummaryResponse(
        Long id,
        Long scholarshipId,
        String scholarshipTitle,
        Long studentId,
        String studentName,
        Long reviewerId,
        String reviewerName,
        ApplicationStatus status,
        Instant submittedAt,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
