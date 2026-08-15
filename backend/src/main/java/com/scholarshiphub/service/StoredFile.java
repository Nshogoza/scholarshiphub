package com.scholarshiphub.service;

/** Result of persisting an uploaded file, ready to be recorded as an
 *  {@code application_documents} row. */
public record StoredFile(
        String storedPath,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksumSha256
) {
}
