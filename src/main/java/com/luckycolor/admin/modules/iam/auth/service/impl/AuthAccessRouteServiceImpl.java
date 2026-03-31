package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.config.AuthAccessProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthAccessRouteService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthAccessRouteServiceImpl implements AuthAccessRouteService {

    private final AuthAccessProperties authAccessProperties;

    public AuthAccessRouteServiceImpl(AuthAccessProperties authAccessProperties) {
        this.authAccessProperties = authAccessProperties;
    }

    @Override
    public List<AuthRouteResponse> getAccessibleRoutes(AuthUser user) {
        return filterRoutes(authAccessProperties.getRoutes(), user, null);
    }

    @Override
    public AuthAccessSnapshotResponse getAccessSnapshot(AuthUser user) {
        List<AuthRouteResponse> routes = getAccessibleRoutes(user);
        return new AuthAccessSnapshotResponse(
            user.userId(),
            user.tenantId(),
            user.roles(),
            user.permissions(),
            collectRouteCodes(routes),
            resolveHomePath(routes)
        );
    }

    private List<AuthRouteResponse> filterRoutes(
        List<AuthAccessProperties.Route> routes,
        AuthUser user,
        String parentPath
    ) {
        List<AuthRouteResponse> accessibleRoutes = new ArrayList<>();
        for (AuthAccessProperties.Route route : routes) {
            String fullPath = joinPath(parentPath, route.getPath());
            List<AuthRouteResponse> childRoutes = filterRoutes(route.getChildren(), user, fullPath);
            boolean currentRouteAccessible = hasRequiredRole(route, user) && hasRequiredPermission(route, user);
            if (!currentRouteAccessible && childRoutes.isEmpty()) {
                continue;
            }
            accessibleRoutes.add(
                new AuthRouteResponse(
                    route.getCode(),
                    route.getName(),
                    route.getPath(),
                    fullPath,
                    route.getComponent(),
                    route.getRedirect(),
                    route.getIcon(),
                    route.isHidden(),
                    route.isAlwaysShow(),
                    route.isKeepAlive(),
                    safeList(route.getPermissions()),
                    childRoutes
                )
            );
        }
        return accessibleRoutes;
    }

    private boolean hasRequiredRole(AuthAccessProperties.Route route, AuthUser user) {
        if (route.getRoles().isEmpty()) {
            return true;
        }
        return route.getRoles().stream().anyMatch(user.roles()::contains);
    }

    private boolean hasRequiredPermission(AuthAccessProperties.Route route, AuthUser user) {
        if (route.getPermissions().isEmpty()) {
            return true;
        }
        return route.getPermissions().stream().anyMatch(user.permissions()::contains);
    }

    private List<String> collectRouteCodes(List<AuthRouteResponse> routes) {
        Set<String> routeCodes = new LinkedHashSet<>();
        collectRouteCodesRecursively(routes, routeCodes);
        return new ArrayList<>(routeCodes);
    }

    private void collectRouteCodesRecursively(List<AuthRouteResponse> routes, Set<String> routeCodes) {
        for (AuthRouteResponse route : routes) {
            if (StringUtils.hasText(route.code())) {
                routeCodes.add(route.code());
            }
            collectRouteCodesRecursively(route.children(), routeCodes);
        }
    }

    private String resolveHomePath(List<AuthRouteResponse> routes) {
        for (AuthRouteResponse route : routes) {
            if (!route.children().isEmpty()) {
                String childHomePath = resolveHomePath(route.children());
                if (StringUtils.hasText(childHomePath)) {
                    return childHomePath;
                }
            }
            if (StringUtils.hasText(route.fullPath()) && !route.hidden()) {
                return route.fullPath();
            }
        }
        return null;
    }

    private String joinPath(String parentPath, String currentPath) {
        if (!StringUtils.hasText(currentPath)) {
            return StringUtils.hasText(parentPath) ? parentPath : null;
        }
        if (currentPath.startsWith("/")) {
            return normalizePath(currentPath);
        }
        if (!StringUtils.hasText(parentPath)) {
            return normalizePath(currentPath);
        }
        return normalizePath(parentPath + "/" + currentPath);
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        String normalizedPath = path.trim().replace('\\', '/');
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return normalizedPath.replaceAll("/+", "/");
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
