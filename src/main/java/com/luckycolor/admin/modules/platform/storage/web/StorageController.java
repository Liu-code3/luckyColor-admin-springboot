package com.luckycolor.admin.modules.platform.storage.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FILE_NOT_FOUND;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.REQUEST_PARAMETER_INVALID;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.service.StoredFile;
import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/files")
@Tag(name = "File Storage", description = "Upload and download file APIs")
public class StorageController {

    private final FileStorageService fileStorageService;

    public StorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("file:upload")
    @Operation(summary = "Upload a file")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Upload successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\":0,\"message\":\"ok\",\"data\":{\"originalFilename\":\"avatar.png\",\"storedFilename\":\"20260402/3f2e1c-avatar.png\",\"relativePath\":\"20260402/3f2e1c-avatar.png\",\"size\":24567,\"contentType\":\"image/png\",\"downloadUrl\":\"/api/admin/files/download?path=20260402/3f2e1c-avatar.png\"},\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Missing file or invalid multipart request",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = REQUEST_PARAMETER_INVALID
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = FORBIDDEN
            ))
        )
    })
    public ApiResponse<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.upload(file));
    }

    @GetMapping("/download")
    @RequirePermission("file:download")
    @Operation(summary = "Download a file by storage path")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Binary file stream",
            content = @Content(
                mediaType = "application/octet-stream",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Missing or invalid path parameter",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = REQUEST_PARAMETER_INVALID
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = FORBIDDEN
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "File not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = FILE_NOT_FOUND
            ))
        )
    })
    public ResponseEntity<Resource> download(@RequestParam("path") String path) {
        StoredFile storedFile = fileStorageService.download(path);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (storedFile.contentType() != null) {
            mediaType = MediaType.parseMediaType(storedFile.contentType());
        }
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(storedFile.storedFilename(), StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .contentType(mediaType)
            .contentLength(storedFile.size())
            .body(storedFile.resource());
    }
}
