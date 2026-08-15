package com.scholarshiphub.dto.request;

import jakarta.validation.constraints.NotNull;

public record ApplicationCreateRequest(

        @NotNull(message = "Scholarship id is required")
        Long scholarshipId
) {
}
