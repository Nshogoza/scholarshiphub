package com.scholarshiphub.service.impl;

import com.scholarshiphub.config.AppProperties;
import com.scholarshiphub.exception.FileValidationException;
import com.scholarshiphub.service.StorageService;
import com.scholarshiphub.service.StoredFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores files on local disk under {@code app.upload.directory}, expected to
 * be a Docker volume in deployment so uploads survive container restarts.
 * Every path segment is derived from a generated UUID, never from the
 * client-supplied filename, so path traversal via a crafted filename is
 * structurally impossible.
 */
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final AppProperties appProperties;

    @Override
    public StoredFile store(MultipartFile file, String subDirectory) {
        validate(file);

        try {
            Path rootDir = Paths.get(appProperties.upload().directory()).toAbsolutePath().normalize();
            Path targetDir = rootDir.resolve(subDirectory).normalize();
            if (!targetDir.startsWith(rootDir)) {
                throw new FileValidationException("Invalid storage sub-directory");
            }
            Files.createDirectories(targetDir);

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + (extension != null ? "." + extension : "");
            Path targetPath = targetDir.resolve(storedFilename);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream();
                 DigestInputStream digestStream = new DigestInputStream(in, digest)) {
                Files.copy(digestStream, targetPath);
            }

            String checksum = HexFormat.of().formatHex(digest.digest());
            String relativePath = rootDir.relativize(targetPath).toString().replace('\\', '/');

            return new StoredFile(relativePath, file.getOriginalFilename(), file.getContentType(),
                    file.getSize(), checksum);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Failed to store uploaded file", e);
            throw new FileValidationException("Failed to store the uploaded file");
        }
    }

    @Override
    public Resource loadAsResource(String storedPath) {
        try {
            Path rootDir = Paths.get(appProperties.upload().directory()).toAbsolutePath().normalize();
            Path filePath = rootDir.resolve(storedPath).normalize();
            if (!filePath.startsWith(rootDir)) {
                throw new FileValidationException("Invalid file path");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileValidationException("Stored file is missing or unreadable");
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new FileValidationException("Invalid file path");
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            Path rootDir = Paths.get(appProperties.upload().directory()).toAbsolutePath().normalize();
            Path filePath = rootDir.resolve(storedPath).normalize();
            if (filePath.startsWith(rootDir)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete stored file {}: {}", storedPath, e.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("No file was provided");
        }
        if (file.getSize() > appProperties.upload().maxFileSizeBytes()) {
            throw new FileValidationException("File exceeds the maximum allowed size of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !appProperties.upload().allowedContentTypesList().contains(contentType)) {
            throw new FileValidationException(
                    "Unsupported file type. Only PDF and DOCX documents are accepted.");
        }
    }
}
