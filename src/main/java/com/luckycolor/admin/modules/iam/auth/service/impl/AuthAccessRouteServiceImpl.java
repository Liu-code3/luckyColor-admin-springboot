package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.config.AuthAccessProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthAccessRouteService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
                    fullPath,
                    route.getName(),
                    route.getComponent(),
                    route.getRedirect(),
                    buildRouteMeta(route),
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
            String routeCode = resolveRouteCode(route);
            if (StringUtils.hasText(routeCode)) {
                routeCodes.add(routeCode);
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
            if (StringUtils.hasText(route.path()) && !isHidden(route)) {
                return route.path();
            }
        }
        return null;
    }

    private Map<String, Object> buildRouteMeta(AuthAccessProperties.Route route) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", route.getName());
        putIfHasText(meta, "icon", route.getIcon());
        meta.put("hidden", route.isHidden());
        meta.put("menuKey", route.getCode());
        meta.put("permissionCode", resolvePermissionCode(route));
        meta.put("type", route.getChildren().isEmpty() ? 2 : 1);
        meta.put("layout", "default");
        meta.put("keepAlive", route.isKeepAlive());
        if (route.isAlwaysShow()) {
            meta.put("alwaysShow", true);
        }
        return meta;
    }

    private void putIfHasText(Map<String, Object> values, String key, String value) {
        if (StringUtils.hasText(value)) {
            values.put(key, value);
        }
    }

    private String resolvePermissionCode(AuthAccessProperties.Route route) {
        if (route == null) {
            return null;
        }
        if (route.getPermissions() != null && !route.getPermissions().isEmpty()) {
            return route.getPermissions().get(0);
        }
        return route.getCode();
    }

    private String resolveRouteCode(AuthRouteResponse route) {
        if (route == null || route.meta() == null) {
            return null;
        }
        Object menuKey = route.meta().get("menuKey");
        return menuKey instanceof String value && StringUtils.hasText(value) ? value : null;
    }

    private boolean isHidden(AuthRouteResponse route) {
        if (route == null || route.meta() == null) {
            return false;
        }
        Object hidden = route.meta().get("hidden");
        return hidden instanceof Boolean value && value;
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
}
