package com.luckycolor.admin.infrastructure.security.authorization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

class PermissionGuardTest {

    @Test
    void shouldAllowSuperAdmin() {
        AuthUserService authUserService = Mockito.mock(AuthUserService.class);
        when(authUserService.getByUserId(1L)).thenReturn(
            new AuthUser(1L, "admin", null, 1L, "Admin", 0, List.of("ROLE_SUPER_ADMIN"), List.of())
        );
        PermissionGuard permissionGuard = new PermissionGuard(authUserService);

        permissionGuard.checkPermission(buildAuthentication(1L), "tenant:query");
    }

    @Test
    void shouldRejectWhenPermissionMissing() {
        AuthUserService authUserService = Mockito.mock(AuthUserService.class);
        when(authUserService.getByUserId(2L)).thenReturn(
            new AuthUser(2L, "viewer", null, 1L, "Viewer", 0, List.of("ROLE_VIEWER"), List.of("system:user:query"))
        );
        PermissionGuard permissionGuard = new PermissionGuard(authUserService);

        assertThatThrownBy(() -> permissionGuard.checkPermission(buildAuthentication(2L), "tenant:query"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Forbidden");
    }

    @Test
    void shouldRejectWhenAuthenticationMissing() {
        PermissionGuard permissionGuard = new PermissionGuard(Mockito.mock(AuthUserService.class));

        assertThatThrownBy(() -> permissionGuard.checkPermission((Authentication) null, "tenant:query"))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
            .hasMessageContaining("Unauthorized");
    }

    private Authentication buildAuthentication(Long userId) {
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(userId, "tester", 1L, List.of("ROLE_VIEWER"));
        return new UsernamePasswordAuthenticationToken(principal, "jwt-token", List.of());
    }
}
