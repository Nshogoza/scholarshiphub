package com.scholarshiphub.dto.response;

import com.scholarshiphub.entity.enums.ScholarshipStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ScholarshipResponse(
        Long id,
        String title,
        String description,
        String eligibilityCriteria,
        BigDecimal amount,
        Instant applicationDeadline,
        ScholarshipStatus status,
        List<RequiredDocumentResponse> requiredDocuments,
        String createdByName,
        Instant createdAt,
        Instant updatedAt
) {
}
