package com.scholarshiphub.dto.response;

import com.scholarshiphub.entity.enums.ReviewRecommendation;
import java.math.BigDecimal;
import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long reviewerId,
        String reviewerName,
        BigDecimal score,
        String comments,
        ReviewRecommendation recommendation,
        Instant createdAt
) {
}
