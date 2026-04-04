package com.luckycolor.admin.modules.platform.health.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnVersionInfo() throws Exception {
        mockMvc.perform(get("/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.applicationName").value("luckycolor-admin-springboot-test"))
            .andExpect(jsonPath("$.data.version").value("1.0.0-alpha.1"))
            .andExpect(jsonPath("$.data.releaseStage").value("alpha"))
            .andExpect(jsonPath("$.data.docsPath").value("/api/docs"));
    }
}
