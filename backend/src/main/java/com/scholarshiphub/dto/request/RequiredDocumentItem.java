package com.scholarshiphub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequiredDocumentItem(

        @NotBlank(message = "Document name is required")
        @Size(max = 255, message = "Document name must not exceed 255 characters")
        String documentName,

        boolean mandatory
) {
}
