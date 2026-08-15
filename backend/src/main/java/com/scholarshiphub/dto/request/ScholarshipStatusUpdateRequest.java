package com.scholarshiphub.dto.request;

import com.scholarshiphub.entity.enums.ScholarshipStatus;
import jakarta.validation.constraints.NotNull;

public record ScholarshipStatusUpdateRequest(

        @NotNull(message = "Status is required")
        ScholarshipStatus status
) {
}
