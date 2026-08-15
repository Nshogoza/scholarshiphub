package com.scholarshiphub.service;

import org.springframework.core.io.Resource;

public record DownloadableDocument(Resource resource, String filename, String contentType) {
}
