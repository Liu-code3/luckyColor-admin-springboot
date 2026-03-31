package com.luckycolor.admin.modules.iam.auth.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    @Test
    void shouldReturnCaptcha() throws Exception {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaService loginCaptchaService = Mockito.mock(LoginCaptchaService.class);
        LoginCaptchaProperties loginCaptchaProperties = new LoginCaptchaProperties();
        when(loginCaptchaService.createCaptcha()).thenReturn(
            new LoginCaptchaResponse("captcha-1", "data:image/svg+xml;base64,abc")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new AuthController(authService, loginCaptchaService, loginCaptchaProperties)
        ).build();

        mockMvc.perform(get("/auth/captcha"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.captchaKey").value("captcha-1"))
            .andExpect(jsonPath("$.data.captchaImage").value("data:image/svg+xml;base64,abc"));
    }

    @Test
    void shouldLogin() throws Exception {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaProperties loginCaptchaProperties = new LoginCaptchaProperties();
        when(authService.login(any())).thenReturn(
            new AuthLoginResponse(
                "jwt-token",
                "Bearer",
                7200,
                1L,
                "admin",
                "System Admin",
                1L,
                java.util.List.of("ROLE_SUPER_ADMIN")
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new AuthController(authService, null, loginCaptchaProperties)
        ).build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin123","captchaKey":"captcha-1","captchaCode":"ABCD"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.tenantId").value(1));
    }
}
