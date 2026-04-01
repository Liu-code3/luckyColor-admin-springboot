package com.luckycolor.admin.modules.system.config.web;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigDetailResponse;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemConfigControllerTest {

    @Test
    void shouldReturnSystemConfigPage() throws Exception {
        SystemConfigService service = Mockito.mock(SystemConfigService.class);
        Mockito.when(service.pageConfigs(any())).thenReturn(PageResult.of(List.of(
            new SystemConfigPageResponse(1L, 1L, "sms.secret", "SMS Secret", "******", 1, 0, 1, "default")
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemConfigController(service)).build();

        mockMvc.perform(get("/admin/system-configs/page").param("configKey", "sms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].configKey").value("sms.secret"))
            .andExpect(jsonPath("$.data.list[0].configValue").value("******"));
    }

    @Test
    void shouldReturnSystemConfigDetail() throws Exception {
        SystemConfigService service = Mockito.mock(SystemConfigService.class);
        Mockito.when(service.getConfig(1L)).thenReturn(
            new SystemConfigDetailResponse(1L, 1L, "sms.secret", "SMS Secret", "******", 1, 0, 1, "default")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemConfigController(service)).build();

        mockMvc.perform(get("/admin/system-configs/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.configName").value("SMS Secret"));
    }

    @Test
    void shouldCreateSystemConfig() throws Exception {
        SystemConfigService service = Mockito.mock(SystemConfigService.class);
        Mockito.when(service.createConfig(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemConfigController(service)).build();

        mockMvc.perform(post("/admin/system-configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"configKey":"sms.secret","configName":"SMS Secret","configValue":"raw-secret","sensitive":1,"status":0,"sort":1,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateSystemConfig() throws Exception {
        SystemConfigService service = Mockito.mock(SystemConfigService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemConfigController(service)).build();

        mockMvc.perform(put("/admin/system-configs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"configKey":"sms.secret","configName":"SMS Secret","configValue":"raw-secret","sensitive":1,"status":0,"sort":1,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }
}
