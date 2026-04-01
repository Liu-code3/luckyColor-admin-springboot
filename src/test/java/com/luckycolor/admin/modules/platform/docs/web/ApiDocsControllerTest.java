package com.luckycolor.admin.modules.platform.docs.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiDocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderDocsPage() throws Exception {
        mockMvc.perform(get("/docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("/docs/assets/swagger-ui.css")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("/v3/api-docs")));
    }

    @Test
    void shouldRenderDocsPageWithTrailingSlash() throws Exception {
        mockMvc.perform(get("/docs/"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    void shouldAllowDocsAssetsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/docs/assets/swagger-ui.css"))
            .andExpect(status().isOk());
    }
}
