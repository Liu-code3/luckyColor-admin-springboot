package com.luckycolor.admin.modules.platform.storage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.luckycolor.admin.modules.platform.storage.config.StorageProperties;
import com.luckycolor.admin.modules.platform.storage.service.impl.FileStorageServiceImpl;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldUploadFile() throws Exception {
        FileStorageService service = new FileStorageServiceImpl(storageProperties());
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            "image/png",
            "png-data".getBytes(StandardCharsets.UTF_8)
        );

        var result = service.upload(file);

        assertThat(result.originalFilename()).isEqualTo("avatar.png");
        assertThat(result.relativePath()).contains("avatar.png");
        assertThat(Files.exists(tempDir.resolve(result.relativePath()))).isTrue();
    }

    @Test
    void shouldDownloadStoredFile() {
        FileStorageService service = new FileStorageServiceImpl(storageProperties());
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "document.txt",
            "text/plain",
            "hello".getBytes(StandardCharsets.UTF_8)
        );
        var uploaded = service.upload(file);

        var storedFile = service.download(uploaded.relativePath());

        assertThat(storedFile.storedFilename()).contains("document.txt");
        assertThat(storedFile.size()).isEqualTo(5L);
    }

    @Test
    void shouldRejectTraversalPath() {
        FileStorageService service = new FileStorageServiceImpl(storageProperties());

        assertThatThrownBy(() -> service.download("../secret.txt"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");
    }

    private StorageProperties storageProperties() {
        StorageProperties properties = new StorageProperties();
        properties.setRootPath(tempDir.toString());
        return properties;
    }
}
