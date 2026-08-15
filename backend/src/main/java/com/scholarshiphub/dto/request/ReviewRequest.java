package com.scholarshiphub.dto.request;

import com.scholarshiphub.entity.enums.ReviewRecommendation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ReviewRequest(

        @DecimalMin(value = "0.0", message = "Score cannot be negative")
        @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
        BigDecimal score,

        @Size(max = 5000)
        String comments,

        @NotNull(message = "Recommendation is required")
        ReviewRecommendation recommendation
) {
}
