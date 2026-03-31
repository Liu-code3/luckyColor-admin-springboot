package com.luckycolor.admin.modules.iam.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
            new AuthController(authService, loginCaptchaService, loginCaptchaProperties, Mockito.mock(JwtTokenService.class))
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
                List.of("ROLE_SUPER_ADMIN")
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new AuthController(authService, null, loginCaptchaProperties, Mockito.mock(JwtTokenService.class))
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

    @Test
    void shouldReturnProfile() {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaProperties loginCaptchaProperties = new LoginCaptchaProperties();
        when(authService.getProfile(any())).thenReturn(
            new AuthProfileResponse(1L, "admin", "System Admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );
        AuthController controller = new AuthController(
            authService,
            null,
            loginCaptchaProperties,
            Mockito.mock(JwtTokenService.class)
        );

        Authentication authentication = buildAuthentication();
        AuthProfileResponse response = controller.profile(authentication).data();

        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.roles()).containsExactly("ROLE_SUPER_ADMIN");
    }

    @Test
    void shouldReturnPermissionSnapshot() {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaProperties loginCaptchaProperties = new LoginCaptchaProperties();
        when(authService.getPermissionSnapshot(any())).thenReturn(
            new AuthPermissionSnapshotResponse(1L, 1L, List.of("ROLE_SUPER_ADMIN"), List.of("system:user:query"))
        );
        AuthController controller = new AuthController(
            authService,
            null,
            loginCaptchaProperties,
            Mockito.mock(JwtTokenService.class)
        );

        Authentication authentication = buildAuthentication();
        AuthPermissionSnapshotResponse response = controller.permissions(authentication).data();

        assertThat(response.permissions()).containsExactly("system:user:query");
    }

    @Test
    void shouldLogout() {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaProperties loginCaptchaProperties = new LoginCaptchaProperties();
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        when(jwtTokenService.resolveBearerToken("Bearer jwt-token")).thenReturn("jwt-token");
        AuthController controller = new AuthController(authService, null, loginCaptchaProperties, jwtTokenService);

        Boolean response = controller.logout(buildAuthentication(), "Bearer jwt-token", new org.springframework.mock.web.MockHttpServletRequest()).data();

        assertThat(response).isTrue();
        Mockito.verify(authService).logout(any(), eq("jwt-token"), any());
    }

    private Authentication buildAuthentication() {
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"));
        return new UsernamePasswordAuthenticationToken(principal, "jwt-token", List.of());
    }
}
