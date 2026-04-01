package com.luckycolor.admin.modules.platform.storage.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.service.StoredFile;
import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class StorageControllerTest {

    @Test
    void shouldUploadFile() throws Exception {
        FileStorageService service = Mockito.mock(FileStorageService.class);
        when(service.upload(any())).thenReturn(
            new FileUploadResponse(
                "avatar.png",
                "stored-avatar.png",
                "2026/04/01/stored-avatar.png",
                8L,
                "image/png",
                "/api/admin/files/download?path=2026/04/01/stored-avatar.png"
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StorageController(service)).build();
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "png-data".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/admin/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.originalFilename").value("avatar.png"))
            .andExpect(jsonPath("$.data.relativePath").value("2026/04/01/stored-avatar.png"));
    }

    @Test
    void shouldDownloadFile() throws Exception {
        FileStorageService service = Mockito.mock(FileStorageService.class);
        when(service.download(eq("2026/04/01/stored-avatar.png"))).thenReturn(
            new StoredFile(
                new ByteArrayResource("png-data".getBytes(StandardCharsets.UTF_8)),
                "stored-avatar.png",
                8L,
                "image/png"
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StorageController(service)).build();

        mockMvc.perform(get("/admin/files/download").param("path", "2026/04/01/stored-avatar.png"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("stored-avatar.png")));
    }
}
