package com.scholarshiphub.dto.response;

import java.time.Instant;

public record ApplicationDocumentResponse(
        Long id,
        String documentName,
        String originalFilename,
        String contentType,
        long fileSizeBytes,
        Instant uploadedAt
) {
}
