package com.luckycolor.admin.infrastructure.security.authorization;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PermissionGuard {

    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final AuthUserService authUserService;

    public PermissionGuard(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    public void checkPermission(String permission) {
        AuthUser user = getRequiredUser();
        if (!hasPermission(user, permission)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    public void checkPermission(Authentication authentication, String permission) {
        AuthUser user = resolveUser(authentication);
        if (!hasPermission(user, permission)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    public void checkAnyPermission(String[] permissions) {
        AuthUser user = getRequiredUser();
        if (!hasAnyPermission(user, permissions)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    public void checkAnyPermission(Authentication authentication, String[] permissions) {
        AuthUser user = resolveUser(authentication);
        if (!hasAnyPermission(user, permissions)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    public boolean hasPermission(Authentication authentication, String permission) {
        return hasPermission(resolveUser(authentication), permission);
    }

    public boolean hasAnyPermission(Authentication authentication, String[] permissions) {
        return hasAnyPermission(resolveUser(authentication), permissions);
    }

    private AuthUser getRequiredUser() {
        return resolveUser(SecurityContextHolder.getContext().getAuthentication());
    }

    private AuthUser resolveUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }
        AuthUser user = authUserService.getByUserId(principal.userId());
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }
        return user;
    }

    private boolean hasPermission(AuthUser user, String permission) {
        if (!StringUtils.hasText(permission)) {
            return true;
        }
        return isSuperAdmin(user) || user.permissions().contains(permission.trim());
    }

    private boolean hasAnyPermission(AuthUser user, String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        return isSuperAdmin(user)
            || Arrays.stream(permissions)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch(user.permissions()::contains);
    }

    private boolean isSuperAdmin(AuthUser user) {
        List<String> roles = user.roles();
        return roles != null && roles.contains(SUPER_ADMIN_ROLE);
    }
}
