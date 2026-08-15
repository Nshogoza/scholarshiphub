package com.scholarshiphub.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignReviewerRequest(

        @NotNull(message = "Reviewer id is required")
        Long reviewerId
) {
}
