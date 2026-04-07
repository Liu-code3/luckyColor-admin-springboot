package com.luckycolor.admin.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = LuckycolorAdminSpringbootApplication.class)
@AutoConfigureMockMvc
class FileAccessSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectAnonymousLegacyFileReadRequest() throws Exception {
        mockMvc.perform(get("/api/file/2026/04/03/avatar.png").header("x-tenant-id", "1001"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void shouldRejectAnonymousProtectedDownloadRequest() throws Exception {
        mockMvc.perform(get("/api/admin/files/download")
                .param("path", "2026/04/03/avatar.png")
                .header("x-tenant-id", "1001"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Unauthorized"));
    }
}
