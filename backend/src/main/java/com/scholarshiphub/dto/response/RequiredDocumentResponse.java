package com.scholarshiphub.dto.response;

public record RequiredDocumentResponse(
        Long id,
        String documentName,
        boolean mandatory
) {
}
