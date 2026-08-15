package com.scholarshiphub.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over "where uploaded documents physically live". The only
 * implementation today is local-disk ({@code LocalFileStorageService}); a
 * future S3/object-storage backend is a new implementation of this
 * interface, not a rewrite of the application/document service layer.
 */
public interface StorageService {

    /**
     * Validates (content type + size) and persists the file under the given
     * logical sub-directory, returning metadata to record alongside the
     * application. Throws {@link com.scholarshiphub.exception.FileValidationException}
     * if the file fails validation.
     */
    StoredFile store(MultipartFile file, String subDirectory);

    Resource loadAsResource(String storedPath);

    void delete(String storedPath);
}
