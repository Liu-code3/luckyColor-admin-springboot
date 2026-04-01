package com.luckycolor.admin.modules.platform.preference.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.preference.service.UserPreferenceService;
import com.luckycolor.admin.modules.platform.preference.web.response.UserPreferenceResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserPreferenceControllerTest {

    @Test
    void shouldReturnCurrentPreference() throws Exception {
        UserPreferenceService service = Mockito.mock(UserPreferenceService.class);
        Mockito.when(service.getCurrentPreference(1L, 1L)).thenReturn(response());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserPreferenceController(service)).build();

        mockMvc.perform(get("/admin/user-preferences/current").principal(authentication()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.themeScheme").value("dark"));
    }

    @Test
    void shouldSaveCurrentPreference() throws Exception {
        UserPreferenceService service = Mockito.mock(UserPreferenceService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserPreferenceController(service)).build();

        mockMvc.perform(put("/admin/user-preferences/current")
                .principal(authentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"themeScheme":"dark","themeColor":"#13c2c2","layoutMode":"mix","contentWidth":"fixed","tabBar":1,"fixedHeader":1,"fixedSidebar":1,"sidebarCollapsed":1,"compactMode":0,"locale":"en-US"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).saveCurrentPreference(eq(1L), eq(1L), any());
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN")),
            null
        );
    }

    private UserPreferenceResponse response() {
        return new UserPreferenceResponse(1L, 1L, "dark", "#13c2c2", "mix", "fixed", 1, 1, 1, 1, 0, "en-US");
    }
}
