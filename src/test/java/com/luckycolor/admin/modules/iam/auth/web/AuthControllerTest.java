package com.luckycolor.admin.modules.iam.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.error.GlobalExceptionHandler;
import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.service.AuthAntiAbuseService;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LegacyLoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginUserResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaChallengeResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaVerifyResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

class AuthControllerTest {

    @Test
    void shouldReturnCaptcha() throws Exception {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaService loginCaptchaService = Mockito.mock(LoginCaptchaService.class);
        AuthAntiAbuseService authAntiAbuseService = Mockito.mock(AuthAntiAbuseService.class);
        when(loginCaptchaService.createCaptcha()).thenReturn(
            new LoginCaptchaResponse("captcha-1", "data:image/svg+xml;base64,abc")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            buildController(authService, loginCaptchaService, null, Mockito.mock(JwtTokenService.class), authAntiAbuseService)
        ).build();

        mockMvc.perform(get("/auth/captcha"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.captchaKey").value("captcha-1"))
            .andExpect(jsonPath("$.data.captchaImage").value("data:image/svg+xml;base64,abc"));

        verify(authAntiAbuseService).checkCaptchaAllowed("127.0.0.1");
    }

    @Test
    void shouldReturnLegacyCaptchaChallenge() throws Exception {
        AuthService authService = Mockito.mock(AuthService.class);
        LegacyLoginCaptchaService legacyLoginCaptchaService = Mockito.mock(LegacyLoginCaptchaService.class);
        AuthAntiAbuseService authAntiAbuseService = Mockito.mock(AuthAntiAbuseService.class);
        when(legacyLoginCaptchaService.createChallenge()).thenReturn(
            new LegacyLoginCaptchaChallengeResponse("challenge-1", "<svg></svg>", "请计算结果", Instant.now())
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            buildController(authService, null, legacyLoginCaptchaService, Mockito.mock(JwtTokenService.class), authAntiAbuseService)
        ).build();

        mockMvc.perform(get("/auth/captcha/challenge"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.captchaId").value("challenge-1"))
            .andExpect(jsonPath("$.data.captchaSvg").value("<svg></svg>"));

        verify(authAntiAbuseService).checkCaptchaAllowed("127.0.0.1");
    }

    @Test
    void shouldReturnTooManyRequestsWhenCaptchaRateLimited() throws Exception {
        AuthService authService = Mockito.mock(AuthService.class);
        LoginCaptchaService loginCaptchaService = Mockito.mock(LoginCaptchaService.class);
        AuthAntiAbuseService authAntiAbuseService = Mockito.mock(AuthAntiAbuseService.class);
        Mockito.doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "AUTH_CAPTCHA_RATE_LIMITED"))
            .when(authAntiAbuseService)
            .checkCaptchaAllowed(any());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                buildController(authService, loginCaptchaService, null, Mockito.mock(JwtTokenService.class), authAntiAbuseService)
            )
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        mockMvc.perform(get("/auth/captcha"))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value(1011012))
            .andExpect(jsonPath("$.message").value("captcha requests are too frequent, please try again later"));
    }

    @Test
    void shouldLogin() throws Exception {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.login(any())).thenReturn(
            new AuthLoginResponse(
                "jwt-token",
                "Bearer",
                7200,
                "refresh-token",
                1L,
                "admin",
                "System Admin",
                "tenant_001",
                List.of("ROLE_SUPER_ADMIN"),
                List.of("dashboard:query"),
                List.of("dashboard:query"),
                List.of("dashboard:query"),
                List.of("dashboard:query"),
                List.of("main_analysis"),
                "TENANT",
                List.of(),
                new AuthLoginUserResponse(
                    1L,
                    "tenant_001",
                    null,
                    "admin",
                    "System Admin",
                    List.of("ROLE_SUPER_ADMIN"),
                    List.of("main_analysis"),
                    List.of("dashboard:query"),
                    List.of("dashboard:query"),
                    List.of("dashboard:query"),
                    List.of("dashboard:query"),
                    "TENANT",
                    List.of()
                )
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            buildController(authService, null, null, Mockito.mock(JwtTokenService.class))
        ).build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"123456","captchaKey":"captcha-1","captchaCode":"ABCD"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.tenantId").value("tenant_001"));
    }

    @Test
    void shouldReturnProfile() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getProfile(any())).thenReturn(
            new AuthProfileResponse(
                1L,
                1L,
                "admin",
                "System Admin",
                "tenant_001",
                null,
                List.of("ROLE_SUPER_ADMIN"),
                List.of("ROLE_SUPER_ADMIN"),
                List.of("main_system_users"),
                List.of("system:user:query"),
                List.of("system:user:query"),
                List.of("system:user:query"),
                List.of("system:user:query"),
                "TENANT",
                List.of()
            )
        );
        AuthController controller = buildController(authService, null, null, Mockito.mock(JwtTokenService.class));

        Authentication authentication = buildAuthentication();
        AuthProfileResponse response = controller.profile(authentication).data();

        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.roles()).containsExactly("ROLE_SUPER_ADMIN");
    }

    @Test
    void shouldReturnPermissionSnapshot() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getPermissionSnapshot(any())).thenReturn(
            new AuthPermissionSnapshotResponse(1L, "tenant_001", List.of("ROLE_SUPER_ADMIN"), List.of("system:user:query"))
        );
        AuthController controller = buildController(authService, null, null, Mockito.mock(JwtTokenService.class));

        Authentication authentication = buildAuthentication();
        AuthPermissionSnapshotResponse response = controller.permissions(authentication).data();

        assertThat(response.permissions()).containsExactly("system:user:query");
    }

    @Test
    void shouldLogout() {
        AuthService authService = Mockito.mock(AuthService.class);
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        when(jwtTokenService.resolveBearerToken("Bearer jwt-token")).thenReturn("jwt-token");
        AuthController controller = buildController(authService, null, null, jwtTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new MockCookie("lc_refresh_token", "refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        Boolean result = controller.logout(buildAuthentication(), "Bearer jwt-token", request, response).data();

        assertThat(result).isTrue();
        verify(authService).logout(any(), eq("jwt-token"), eq("refresh-token"), any());
        assertThat(response.getHeader("Set-Cookie")).contains("lc_refresh_token=");
    }

    @Test
    void shouldReturnRoutes() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getRoutes(any())).thenReturn(
            List.of(
                new AuthRouteResponse(
                    "/dashboard",
                    "Dashboard",
                    "dashboard/index",
                    null,
                    Map.of(
                        "title", "Dashboard",
                        "hidden", false,
                        "menuKey", "dashboard",
                        "permissionCode", "dashboard",
                        "type", 2,
                        "layout", "default",
                        "keepAlive", true
                    ),
                    List.of()
                )
            )
        );
        AuthController controller = buildController(authService, null, null, Mockito.mock(JwtTokenService.class));

        List<AuthRouteResponse> response = controller.routes(buildAuthentication()).data();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).path()).isEqualTo("/dashboard");
        assertThat(response.get(0).meta()).containsEntry("menuKey", "dashboard");
    }

    @Test
    void shouldReturnAccessSnapshot() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getAccessSnapshot(any())).thenReturn(
            new AuthAccessSnapshotResponse(
                new AuthAccessSnapshotResponse.AuthAccessUserResponse(
                    1L,
                    "tenant_001",
                    "admin",
                    "System Admin",
                    List.of("ROLE_SUPER_ADMIN"),
                    List.of("dashboard", "system:user"),
                    List.of("system:user:query")
                ),
                List.of(
                    new AuthAccessSnapshotResponse.AuthAccessRoleResponse(
                        "tenant_001",
                        "1",
                        "ROLE_SUPER_ADMIN",
                        "ROLE_SUPER_ADMIN"
                    )
                ),
                List.of(
                    new AuthAccessSnapshotResponse.AuthAccessMenuTreeItemResponse(
                        0L,
                        1L,
                        "Dashboard",
                        "Dashboard",
                        2,
                        "/dashboard",
                        "dashboard",
                        "dashboard",
                        "",
                        "default",
                        true,
                        true,
                        "dashboard/index",
                        null,
                        Map.of("title", "Dashboard", "menuKey", "dashboard"),
                        1,
                        null,
                        null,
                        null
                    )
                )
            )
        );
        AuthController controller = buildController(authService, null, null, Mockito.mock(JwtTokenService.class));

        AuthAccessSnapshotResponse response = controller.access(buildAuthentication()).data();

        assertThat(response.user().menuCodeList()).containsExactly("dashboard", "system:user");
        assertThat(response.roles()).extracting(AuthAccessSnapshotResponse.AuthAccessRoleResponse::code)
            .containsExactly("ROLE_SUPER_ADMIN");
        assertThat(response.menuTree()).extracting(AuthAccessSnapshotResponse.AuthAccessMenuTreeItemResponse::key)
            .containsExactly("dashboard");
    }

    private Authentication buildAuthentication() {
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"));
        return new UsernamePasswordAuthenticationToken(principal, "jwt-token", List.of());
    }

    private AuthController buildController(
        AuthService authService,
        LoginCaptchaService loginCaptchaService,
        LegacyLoginCaptchaService legacyLoginCaptchaService,
        JwtTokenService jwtTokenService
    ) {
        return buildController(authService, loginCaptchaService, legacyLoginCaptchaService, jwtTokenService, Mockito.mock(AuthAntiAbuseService.class));
    }

    private AuthController buildController(
        AuthService authService,
        LoginCaptchaService loginCaptchaService,
        LegacyLoginCaptchaService legacyLoginCaptchaService,
        JwtTokenService jwtTokenService,
        AuthAntiAbuseService authAntiAbuseService
    ) {
        return new AuthController(
            authService,
            loginCaptchaService,
            legacyLoginCaptchaService,
            new LoginCaptchaProperties(),
            jwtTokenService,
            buildJwtProperties(),
            authAntiAbuseService
        );
    }

    private SecurityJwtProperties buildJwtProperties() {
        SecurityJwtProperties properties = new SecurityJwtProperties();
        properties.setRefreshCookieName("lc_refresh_token");
        return properties;
    }
}
