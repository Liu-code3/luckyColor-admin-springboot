package com.luckycolor.admin.modules.platform.watermark.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.watermark.service.WatermarkConfigService;
import com.luckycolor.admin.modules.platform.watermark.web.response.WatermarkConfigResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WatermarkConfigControllerTest {

    @Test
    void shouldReturnCurrentConfig() throws Exception {
        WatermarkConfigService service = Mockito.mock(WatermarkConfigService.class);
        Mockito.when(service.getCurrentConfig(1L)).thenReturn(response());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WatermarkConfigController(service)).build();

        mockMvc.perform(get("/admin/watermark-config/current").principal(authentication()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(1))
            .andExpect(jsonPath("$.data.content").value("LuckyColor Tenant"));
    }

    @Test
    void shouldSaveCurrentConfig() throws Exception {
        WatermarkConfigService service = Mockito.mock(WatermarkConfigService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WatermarkConfigController(service)).build();

        mockMvc.perform(put("/admin/watermark-config/current")
                .principal(authentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"enabled":1,"content":"LuckyColor Tenant","color":"#1677ff","fontSize":18,"opacityPercent":20,"rotateDegree":-30,"gapX":140,"gapY":160}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).saveCurrentConfig(eq(1L), any());
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN")),
            null
        );
    }

    private WatermarkConfigResponse response() {
        return new WatermarkConfigResponse(1L, 1, "LuckyColor Tenant", "#1677ff", 18, 20, -30, 140, 160);
    }
}
