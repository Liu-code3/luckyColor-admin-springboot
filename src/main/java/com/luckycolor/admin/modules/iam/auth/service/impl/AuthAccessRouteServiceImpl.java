package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.config.AuthAccessProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthAccessRouteService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse.AuthAccessMenuTreeItemResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse.AuthAccessRoleResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse.AuthAccessUserResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import java.util.ArrayList;
import java.util.Collections;
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
            new AuthAccessUserResponse(
                user.userId(),
                user.tenantId() == null ? null : String.valueOf(user.tenantId()),
                user.username(),
                user.nickname(),
                safeList(user.roles()),
                collectRouteCodes(routes),
                safeList(user.permissions())
            ),
            buildRoleResponses(user),
            buildAccessMenuTree(routes)
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

    private List<AuthAccessRoleResponse> buildRoleResponses(AuthUser user) {
        List<String> roleCodes = safeList(user.roles());
        List<AuthAccessRoleResponse> responses = new ArrayList<>(roleCodes.size());
        for (String roleCode : roleCodes) {
            responses.add(new AuthAccessRoleResponse(user.tenantId() == null ? null : String.valueOf(user.tenantId()), roleCode, roleCode, roleCode));
        }
        return responses;
    }

    private List<AuthAccessMenuTreeItemResponse> buildAccessMenuTree(List<AuthRouteResponse> routes) {
        long[] nextId = {1L};
        return buildAccessMenuTree(routes, 0L, nextId);
    }

    private List<AuthAccessMenuTreeItemResponse> buildAccessMenuTree(
        List<AuthRouteResponse> routes,
        Long parentId,
        long[] nextId
    ) {
        List<AuthAccessMenuTreeItemResponse> items = new ArrayList<>(routes.size());
        for (int index = 0; index < routes.size(); index++) {
            AuthRouteResponse route = routes.get(index);
            long currentId = nextId[0]++;
            Map<String, Object> meta = route.meta() == null ? Map.of() : new LinkedHashMap<>(route.meta());
            List<AuthAccessMenuTreeItemResponse> children = buildAccessMenuTree(route.children(), currentId, nextId);
            items.add(
                new AuthAccessMenuTreeItemResponse(
                    parentId,
                    currentId,
                    resolveTitle(route),
                    route.name(),
                    resolveType(route),
                    route.path(),
                    resolveRouteCode(route),
                    resolvePermissionCode(route),
                    resolveIcon(route),
                    resolveLayout(route),
                    !isHidden(route),
                    true,
                    route.component(),
                    route.redirect(),
                    meta.isEmpty() ? null : meta,
                    index + 1,
                    null,
                    null,
                    children.isEmpty() ? null : children
                )
            );
        }
        return items;
    }

    private Integer resolveType(AuthRouteResponse route) {
        if (route == null || route.meta() == null) {
            return route != null && !route.children().isEmpty() ? 1 : 2;
        }
        Object type = route.meta().get("type");
        if (type instanceof Number number) {
            return number.intValue();
        }
        return !route.children().isEmpty() ? 1 : 2;
    }

    private String resolveTitle(AuthRouteResponse route) {
        if (route != null && route.meta() != null) {
            Object title = route.meta().get("title");
            if (title instanceof String value && StringUtils.hasText(value)) {
                return value;
            }
        }
        return route == null ? null : route.name();
    }

    private String resolvePermissionCode(AuthRouteResponse route) {
        if (route != null && route.meta() != null) {
            Object permissionCode = route.meta().get("permissionCode");
            if (permissionCode instanceof String value && StringUtils.hasText(value)) {
                return value;
            }
        }
        return resolveRouteCode(route);
    }

    private String resolveIcon(AuthRouteResponse route) {
        if (route != null && route.meta() != null) {
            Object icon = route.meta().get("icon");
            if (icon instanceof String value) {
                return value;
            }
        }
        return "";
    }

    private String resolveLayout(AuthRouteResponse route) {
        if (route != null && route.meta() != null) {
            Object layout = route.meta().get("layout");
            if (layout instanceof String value) {
                return value;
            }
        }
        return "";
    }

    private boolean isHidden(AuthRouteResponse route) {
        if (route == null || route.meta() == null) {
            return false;
        }
        Object hidden = route.meta().get("hidden");
        return hidden instanceof Boolean value && value;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? Collections.emptyList() : values;
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
