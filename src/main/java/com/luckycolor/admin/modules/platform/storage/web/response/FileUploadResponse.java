package com.luckycolor.admin.modules.platform.storage.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Uploaded file metadata")
public record FileUploadResponse(
    @Schema(description = "Original filename", example = "avatar.png")
    String originalFilename,
    @Schema(description = "Stored filename", example = "20260402/3f2e1c-avatar.png")
    String storedFilename,
    @Schema(description = "Relative storage path", example = "20260402/3f2e1c-avatar.png")
    String relativePath,
    @Schema(description = "File size in bytes", example = "24567")
    long size,
    @Schema(description = "Detected content type", example = "image/png")
    String contentType,
    @Schema(description = "Download URL", example = "/api/admin/files/download?path=20260402/3f2e1c-avatar.png")
    String downloadUrl
) {
}
