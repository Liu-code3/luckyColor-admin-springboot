package com.luckycolor.admin.modules.platform.storage.web.response;

public record FileUploadResponse(
    String originalFilename,
    String storedFilename,
    String relativePath,
    long size,
    String contentType,
    String downloadUrl
) {
}
