package com.luckycolor.admin.modules.platform.storage.service.impl;

import com.luckycolor.admin.modules.platform.storage.config.StorageProperties;
import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.service.StoredFile;
import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final StorageProperties storageProperties;

    public FileStorageServiceImpl(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload file is required");
        }
        try {
            String originalFilename = resolveOriginalFilename(file.getOriginalFilename());
            String storedFilename = buildStoredFilename(originalFilename);
            String relativePath = DATE_PATH_FORMATTER.format(LocalDate.now()) + "/" + storedFilename;
            Path targetPath = resolveStoragePath(relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
            String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : Files.probeContentType(targetPath);
            String normalizedPath = relativePath.replace('\\', '/');
            return new FileUploadResponse(
                originalFilename,
                storedFilename,
                normalizedPath,
                file.getSize(),
                contentType,
                "/api/admin/files/download?path=" + normalizedPath
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file", exception);
        }
    }

    @Override
    public StoredFile download(String relativePath) {
        try {
            Path filePath = resolveStoragePath(relativePath);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }
            Resource resource = new FileSystemResource(filePath);
            return new StoredFile(
                resource,
                filePath.getFileName().toString(),
                Files.size(filePath),
                Files.probeContentType(filePath)
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load file", exception);
        }
    }

    private Path resolveStoragePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is required");
        }
        Path normalizedRootPath = Paths.get(storageProperties.getRootPath()).toAbsolutePath().normalize();
        Path normalizedRelativePath = Paths.get(relativePath.replace('\\', '/')).normalize();
        if (normalizedRelativePath.isAbsolute() || startsWithParent(normalizedRelativePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is invalid");
        }
        Path resolvedPath = normalizedRootPath.resolve(normalizedRelativePath).normalize();
        if (!resolvedPath.startsWith(normalizedRootPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is invalid");
        }
        return resolvedPath;
    }

    private boolean startsWithParent(Path path) {
        return path.getNameCount() > 0 && "..".equals(path.getName(0).toString());
    }

    private String resolveOriginalFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "upload.bin";
        }
        String cleanedName = Paths.get(originalFilename).getFileName().toString();
        StringBuilder builder = new StringBuilder(cleanedName.length());
        for (int index = 0; index < cleanedName.length(); index++) {
            char currentChar = cleanedName.charAt(index);
            if (Character.isISOControl(currentChar) || currentChar == '/' || currentChar == '\\' || currentChar == ':') {
                builder.append('_');
            } else {
                builder.append(currentChar);
            }
        }
        String sanitizedName = builder.toString().trim();
        return StringUtils.hasText(sanitizedName) ? sanitizedName : "upload.bin";
    }

    private String buildStoredFilename(String originalFilename) {
        return UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;
    }
}
