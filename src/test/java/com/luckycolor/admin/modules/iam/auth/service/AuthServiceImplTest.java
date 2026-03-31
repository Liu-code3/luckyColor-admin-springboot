package com.luckycolor.admin.modules.iam.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.impl.AuthServiceImpl;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import java.util.List;
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
        AuthService authService = new AuthServiceImpl(
            buildAuthProperties("$2a$encoded-password", 0),
            passwordEncoder,
            jwtTokenService,
            buildJwtProperties(),
            buildCaptchaProperties(true),
            loginAuditService,
            loginCaptchaService
        );

        AuthLoginResponse response = authService.login(buildLoginRequest("admin123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.tenantId()).isEqualTo(1L);
        verify(loginCaptchaService).validateCaptcha("captcha-1", "ABCD");
        verify(loginAuditService).recordSuccess("admin", 1L, "127.0.0.1");
    }

    @Test
    void shouldRejectUnknownUser() {
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        AuthService authService = new AuthServiceImpl(
            buildAuthProperties("admin123", 0),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            loginAuditService,
            null
        );
        AuthLoginRequest request = buildLoginRequest("admin123");
        request.setUsername("missing");

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(loginAuditService).recordFailure("missing", null, "127.0.0.1", "USER_NOT_FOUND");
    }

    @Test
    void shouldRejectInvalidPassword() {
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        when(passwordEncoder.matches("wrong", "$2a$encoded-password")).thenReturn(false);
        AuthService authService = new AuthServiceImpl(
            buildAuthProperties("$2a$encoded-password", 0),
            passwordEncoder,
            jwtTokenService,
            buildJwtProperties(),
            buildCaptchaProperties(false),
            loginAuditService,
            null
        );
        AuthLoginRequest request = buildLoginRequest("wrong");

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("401 UNAUTHORIZED");

        verify(loginAuditService).recordFailure("admin", 1L, "127.0.0.1", "PASSWORD_MISMATCH");
        verify(jwtTokenService, never()).createAccessToken(eq(1L), eq("admin"), eq(1L), eq(List.of("ROLE_SUPER_ADMIN")));
    }

    @Test
    void shouldRejectDisabledUser() {
        LoginAuditService loginAuditService = Mockito.mock(LoginAuditService.class);
        AuthService authService = new AuthServiceImpl(
            buildAuthProperties("admin123", 1),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            buildJwtProperties(),
            buildCaptchaProperties(false),
            loginAuditService,
            null
        );

        assertThatThrownBy(() -> authService.login(buildLoginRequest("admin123")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        verify(loginAuditService).recordFailure("admin", 1L, "127.0.0.1", "USER_DISABLED");
    }

    @Test
    void shouldRejectWhenCaptchaServiceUnavailable() {
        AuthService authService = new AuthServiceImpl(
            buildAuthProperties("admin123", 0),
            Mockito.mock(PasswordEncoder.class),
            Mockito.mock(JwtTokenService.class),
            buildJwtProperties(),
            buildCaptchaProperties(true),
            Mockito.mock(LoginAuditService.class),
            null
        );

        assertThatThrownBy(() -> authService.login(buildLoginRequest("admin123")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("503 SERVICE_UNAVAILABLE");
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
