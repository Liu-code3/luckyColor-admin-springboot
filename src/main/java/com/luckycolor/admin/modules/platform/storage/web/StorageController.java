package com.luckycolor.admin.modules.platform.storage.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.service.StoredFile;
import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
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
public class StorageController {

    private final FileStorageService fileStorageService;

    public StorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("file:upload")
    public ApiResponse<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.upload(file));
    }

    @GetMapping("/download")
    @RequirePermission("file:download")
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
