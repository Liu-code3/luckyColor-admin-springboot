package com.luckycolor.admin.modules.iam.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.infrastructure.tenant.service.TenantExternalIdService;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.auth.config.AuthAccessProperties;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.impl.AuthAccessRouteServiceImpl;
import com.luckycolor.admin.modules.iam.auth.service.impl.AuthServiceImpl;
import com.luckycolor.admin.modules.iam.auth.service.impl.InMemoryAuthTokenSessionService;
import com.luckycolor.admin.modules.iam.auth.service.impl.LocalAuthUserServiceImpl;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceImplTest {

    @Test
    void shouldLoginSuccessfully() {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        LoginCaptchaService loginCaptchaService = Mockito.mock(LoginCaptchaService.class);
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        when(passwordEncoder.matches("admin123", "$2a$encoded-password")).thenReturn(true);
        when(jwtTokenService.createAccessToken(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))).thenReturn("jwt-token");
        when(jwtTokenService.createRefreshToken(1L, "admin", 1L)).thenReturn("refresh-token");
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("$2a$encoded-password", 0)),
            passwordEncoder,
            jwtTokenService,
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(true),
            loginAuditService,
            null,
            loginCaptchaService,
            null,
            buildTenantExternalIdService()
        );

        AuthLoginResponse response = authService.login(buildLoginRequest("admin123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.tenantId()).isEqualTo("tenant_001");
        verify(loginCaptchaService).validateCaptcha("captcha-1", "ABCD");
        verify(loginAuditService).recordSuccess(1L, "admin", 1L, "127.0.0.1");
    }

    @Test
    void shouldUseAccessSnapshotButtonCodesForLoginProfileAndPermissionSnapshot() {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        AuthAccessRouteService authAccessRouteService = Mockito.mock(AuthAccessRouteService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        when(passwordEncoder.matches("admin123", "$2a$encoded-password")).thenReturn(true);
        when(jwtTokenService.createAccessToken(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))).thenReturn("jwt-token");
        when(authAccessRouteService.getAccessSnapshot(Mockito.any())).thenReturn(
            new AuthAccessSnapshotResponse(
                new AuthAccessSnapshotResponse.AuthAccessUserResponse(
                    1L,
                    "tenant_001",
                    "admin",
                    "System Admin",
                    List.of("ROLE_SUPER_ADMIN"),
                    List.of("main_system"),
                    List.of("button:from:snapshot")
                ),
                List.of(),
                List.of()
            )
        );
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("$2a$encoded-password", 0)),
            passwordEncoder,
            jwtTokenService,
            new InMemoryAuthTokenSessionService(),
            authAccessRouteService,
            buildJwtProperties(),
            buildCaptchaProperties(false),
            Mockito.mock(LoginAuditService.class),
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        AuthLoginResponse loginResponse = authService.login(buildLoginRequest("admin123"));
        AuthProfileResponse profileResponse = authService.getProfile(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );
        AuthPermissionSnapshotResponse permissionResponse = authService.getPermissionSnapshot(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );

        assertThat(loginResponse.buttonCodeList()).containsExactly("button:from:snapshot");
        assertThat(loginResponse.permissions()).containsExactly("button:from:snapshot");
        assertThat(loginResponse.user().buttonCodeList()).containsExactly("button:from:snapshot");
        assertThat(profileResponse.buttonCodeList()).containsExactly("button:from:snapshot");
        assertThat(profileResponse.permissions()).containsExactly("button:from:snapshot");
        assertThat(permissionResponse.permissions()).containsExactly("button:from:snapshot");
    }

    @Test
    void shouldRejectUnknownUser() {
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            loginAuditService,
            null,
            null,
            null,
            buildTenantExternalIdService()
        );
        AuthLoginRequest request = buildLoginRequest("admin123");
        request.setUsername("missing");

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(loginAuditService).recordFailure(null, "missing", null, "127.0.0.1", "USER_NOT_FOUND");
    }

    @Test
    void shouldRejectInvalidPassword() {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        when(passwordEncoder.matches("wrong", "$2a$encoded-password")).thenReturn(false);
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("$2a$encoded-password", 0)),
            passwordEncoder,
            jwtTokenService,
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            loginAuditService,
            null,
            null,
            null,
            buildTenantExternalIdService()
        );
        AuthLoginRequest request = buildLoginRequest("wrong");

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(loginAuditService).recordFailure(1L, "admin", 1L, "127.0.0.1", "PASSWORD_MISMATCH");
        verify(jwtTokenService, never()).createAccessToken(eq(1L), eq("admin"), eq(1L), eq(List.of("ROLE_SUPER_ADMIN")));
    }

    @Test
    void shouldRejectDisabledUser() {
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 1)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            loginAuditService,
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        assertThatThrownBy(() -> authService.login(buildLoginRequest("admin123")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        verify(loginAuditService).recordFailure(1L, "admin", 1L, "127.0.0.1", "USER_DISABLED");
    }

    @Test
    void shouldRejectWhenCaptchaServiceUnavailable() {
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(true),
            Mockito.mock(LoginAuditService.class),
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        assertThatThrownBy(() -> authService.login(buildLoginRequest("admin123")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("503 SERVICE_UNAVAILABLE");
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        SecurityAuditLogService securityAuditLogService = Mockito.mock(SecurityAuditLogService.class);
        InMemoryAuthTokenSessionService tokenSessionService = new InMemoryAuthTokenSessionService();
        when(jwtTokenService.resolveExpiration("jwt-token")).thenReturn(Instant.now().plusSeconds(3600));
        when(jwtTokenService.resolveRefreshExpiration("refresh-token")).thenReturn(Instant.now().plusSeconds(86400));
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            jwtTokenService,
            tokenSessionService,
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            Mockito.mock(LoginAuditService.class),
            securityAuditLogService,
            null,
            null,
            buildTenantExternalIdService()
        );

        authService.logout(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN")),
            "jwt-token",
            "refresh-token",
            "127.0.0.1"
        );

        assertThat(tokenSessionService.isRevoked("jwt-token")).isTrue();
        assertThat(tokenSessionService.isRevoked("refresh-token")).isTrue();
        verify(securityAuditLogService).recordLogout(1L, "admin", 1L, "127.0.0.1");
    }

    @Test
    void shouldReturnProfile() {
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            Mockito.mock(LoginAuditService.class),
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        AuthProfileResponse response = authService.getProfile(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );

        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.nickname()).isEqualTo("System Admin");
        assertThat(response.tenantId()).isEqualTo("tenant_001");
        assertThat(response.roles()).containsExactly("ROLE_SUPER_ADMIN");
    }

    @Test
    void shouldReturnPermissionSnapshot() {
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            Mockito.mock(LoginAuditService.class),
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        AuthPermissionSnapshotResponse response = authService.getPermissionSnapshot(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );

        assertThat(response.tenantId()).isEqualTo("tenant_001");
        assertThat(response.roles()).containsExactly("ROLE_SUPER_ADMIN");
        assertThat(response.permissions()).containsExactly("system:user:query", "system:user:create");
    }

    @Test
    void shouldReturnAccessibleRoutes() {
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            Mockito.mock(LoginAuditService.class),
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        List<AuthRouteResponse> response = authService.getRoutes(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );

        assertThat(response).hasSize(2);
        assertThat(response.get(0).path()).isEqualTo("/dashboard");
        assertThat(response.get(0).name()).isEqualTo("Dashboard");
        assertThat(response.get(0).meta()).containsEntry("menuKey", "dashboard");
        assertThat(response.get(0).meta()).containsEntry("permissionCode", "dashboard");
        assertThat(response.get(1).path()).isEqualTo("/system");
        assertThat(response.get(1).children()).extracting(AuthRouteResponse::path)
            .containsExactly("/system/users", "/system/roles");
        assertThat(response.get(1).children()).extracting(AuthRouteResponse::meta)
            .containsExactly(
                Map.of(
                    "title", "SystemUser",
                    "hidden", false,
                    "menuKey", "system:user",
                    "permissionCode", "system:user:query",
                    "type", 2,
                    "layout", "default",
                    "keepAlive", false
                ),
                Map.of(
                    "title", "SystemRole",
                    "hidden", false,
                    "menuKey", "system:role",
                    "permissionCode", "system:role",
                    "type", 2,
                    "layout", "default",
                    "keepAlive", false
                )
            );
    }

    @Test
    void shouldReturnAccessSnapshot() {
        AuthService authService = new AuthServiceImpl(
            new LocalAuthUserServiceImpl(buildAuthProperties("admin123", 0)),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            new InMemoryAuthTokenSessionService(),
            new AuthAccessRouteServiceImpl(buildAccessProperties()),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            Mockito.mock(LoginAuditService.class),
            null,
            null,
            null,
            buildTenantExternalIdService()
        );

        AuthAccessSnapshotResponse response = authService.getAccessSnapshot(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"))
        );

        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().roleCodes()).containsExactly("ROLE_SUPER_ADMIN");
        assertThat(response.user().menuCodeList()).containsExactly("dashboard", "system", "system:user", "system:role");
        assertThat(response.user().buttonCodeList()).containsExactly("system:user:query", "system:user:create");
        assertThat(response.roles()).extracting(AuthAccessSnapshotResponse.AuthAccessRoleResponse::code)
            .containsExactly("ROLE_SUPER_ADMIN");
        assertThat(response.menuTree()).hasSize(2);
        assertThat(response.menuTree().get(0).title()).isEqualTo("Dashboard");
        assertThat(response.menuTree().get(0).permissionCode()).isEqualTo("dashboard");
        assertThat(response.menuTree().get(1).children()).extracting(
            AuthAccessSnapshotResponse.AuthAccessMenuTreeItemResponse::permissionCode
        ).containsExactly("system:user:query", "system:role");
    }

    private LocalAuthProperties buildAuthProperties(String password, int status) {
        LocalAuthProperties properties = new LocalAuthProperties();
        LocalAuthProperties.User user = new LocalAuthProperties.User();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setPassword(password);
        user.setTenantId(1L);
        user.setNickname("System Admin");
        user.setStatus(status);
        user.setRoles(List.of("ROLE_SUPER_ADMIN"));
        user.setPermissions(List.of("system:user:query", "system:user:create"));
        user.setDataScope("ALL");
        properties.setLocalUsers(List.of(user));
        return properties;
    }

    private SecurityJwtProperties buildJwtProperties() {
        SecurityJwtProperties properties = new SecurityJwtProperties();
        properties.setExpiresIn("2h");
        return properties;
    }

    private LoginCaptchaProperties buildCaptchaProperties(boolean enabled) {
        LoginCaptchaProperties properties = new LoginCaptchaProperties();
        properties.setEnabled(enabled);
        return properties;
    }

    private TenantExternalIdService buildTenantExternalIdService() {
        return new TenantExternalIdService(null);
    }

    private AuthAccessProperties buildAccessProperties() {
        AuthAccessProperties properties = new AuthAccessProperties();
        AuthAccessProperties.Route dashboard = new AuthAccessProperties.Route();
        dashboard.setCode("dashboard");
        dashboard.setName("Dashboard");
        dashboard.setPath("/dashboard");
        dashboard.setComponent("dashboard/index");

        AuthAccessProperties.Route system = new AuthAccessProperties.Route();
        system.setCode("system");
        system.setName("System");
        system.setPath("/system");
        system.setComponent("Layout");

        AuthAccessProperties.Route userRoute = new AuthAccessProperties.Route();
        userRoute.setCode("system:user");
        userRoute.setName("SystemUser");
        userRoute.setPath("users");
        userRoute.setComponent("system/user/index");
        userRoute.setPermissions(List.of("system:user:query"));

        AuthAccessProperties.Route roleRoute = new AuthAccessProperties.Route();
        roleRoute.setCode("system:role");
        roleRoute.setName("SystemRole");
        roleRoute.setPath("roles");
        roleRoute.setComponent("system/role/index");
        roleRoute.setRoles(List.of("ROLE_SUPER_ADMIN"));

        AuthAccessProperties.Route tenantRoute = new AuthAccessProperties.Route();
        tenantRoute.setCode("tenant:list");
        tenantRoute.setName("TenantList");
        tenantRoute.setPath("/tenants");
        tenantRoute.setComponent("tenant/index");
        tenantRoute.setPermissions(List.of("tenant:query"));

        system.setChildren(List.of(userRoute, roleRoute));
        properties.setRoutes(List.of(dashboard, system, tenantRoute));
        return properties;
    }

    private AuthLoginRequest buildLoginRequest(String password) {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("admin");
        request.setPassword(password);
        request.setCaptchaKey("captcha-1");
        request.setCaptchaCode("ABCD");
        request.setRemoteIp("127.0.0.1");
        return request;
    }
}
