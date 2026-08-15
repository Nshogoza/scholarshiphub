package com.scholarshiphub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ScholarshipRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 20000, message = "Description must not exceed 20000 characters")
        String description,

        @NotBlank(message = "Eligibility criteria is required")
        @Size(max = 20000, message = "Eligibility criteria must not exceed 20000 characters")
        String eligibilityCriteria,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @DecimalMax(value = "100000000.00", message = "Amount must not exceed 100,000,000")
        BigDecimal amount,

        // Deadline-in-the-future is enforced in ScholarshipServiceImpl rather than here:
        // this DTO is reused for updates, and a plain @Future would block editing any
        // other field of a scholarship once its original deadline has naturally passed.
        @NotNull(message = "Application deadline is required")
        Instant applicationDeadline,

        @NotEmpty(message = "At least one required document must be specified")
        List<@Valid RequiredDocumentItem> requiredDocuments
) {
}
