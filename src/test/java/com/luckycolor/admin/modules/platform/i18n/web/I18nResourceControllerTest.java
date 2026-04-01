package com.luckycolor.admin.modules.platform.i18n.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.platform.i18n.service.I18nResourceService;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourceDetailResponse;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourcePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class I18nResourceControllerTest {

    @Test
    void shouldReturnResourcePage() throws Exception {
        I18nResourceService service = Mockito.mock(I18nResourceService.class);
        when(service.pageResources(any())).thenReturn(PageResult.of(List.of(
            new I18nResourcePageResponse(1L, 1L, "en-US", "auth", "user.login.title", "Login", 1, 0, "default")
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new I18nResourceController(service)).build();

        mockMvc.perform(get("/admin/i18n-resources/page").param("locale", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].namespace").value("auth"));
    }

    @Test
    void shouldReturnResourceDetail() throws Exception {
        I18nResourceService service = Mockito.mock(I18nResourceService.class);
        when(service.getResource(1L)).thenReturn(
            new I18nResourceDetailResponse(1L, 1L, "en-US", "auth", "user.login.title", "Login", 1, 0, "default")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new I18nResourceController(service)).build();

        mockMvc.perform(get("/admin/i18n-resources/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.resourceValue").value("Login"));
    }

    @Test
    void shouldCreateResource() throws Exception {
        I18nResourceService service = Mockito.mock(I18nResourceService.class);
        when(service.createResource(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new I18nResourceController(service)).build();

        mockMvc.perform(post("/admin/i18n-resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"locale":"en-US","namespace":"auth","resourceKey":"user.login.title","resourceValue":"Login","status":0,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        I18nResourceService service = Mockito.mock(I18nResourceService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new I18nResourceController(service)).build();

        mockMvc.perform(put("/admin/i18n-resources/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateStatus(eq(1L), any());
    }

    @Test
    void shouldBumpVersion() throws Exception {
        I18nResourceService service = Mockito.mock(I18nResourceService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new I18nResourceController(service)).build();

        mockMvc.perform(put("/admin/i18n-resources/1/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).bumpVersion(1L);
    }
}
