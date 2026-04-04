package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.infrastructure.tenant.service.TenantExternalIdService;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthAccessRouteService;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse.AuthAccessMenuTreeItemResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse.AuthAccessRoleResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse.AuthAccessUserResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.support.FrontendMenuContractMapper;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Primary
@TenantIgnore
@ConditionalOnPersistenceEnabled
public class PersistenceAuthAccessRouteServiceImpl implements AuthAccessRouteService {

    private final SystemRoleMapper systemRoleMapper;
    private final MenuMapper menuMapper;
    private final AuthAccessRouteServiceImpl fallbackRouteService;
    private final ObjectMapper objectMapper;
    private final TenantExternalIdService tenantExternalIdService;

    public PersistenceAuthAccessRouteServiceImpl(
        SystemRoleMapper systemRoleMapper,
        MenuMapper menuMapper,
        AuthAccessRouteServiceImpl fallbackRouteService,
        ObjectMapper objectMapper,
        TenantExternalIdService tenantExternalIdService
    ) {
        this.systemRoleMapper = systemRoleMapper;
        this.menuMapper = menuMapper;
        this.fallbackRouteService = fallbackRouteService;
        this.objectMapper = objectMapper;
        this.tenantExternalIdService = tenantExternalIdService;
    }

    @Override
    public List<AuthRouteResponse> getAccessibleRoutes(AuthUser user) {
        AccessContext context = resolveAccessContext(user);
        if (context.useFallback()) {
            return fallbackRouteService.getAccessibleRoutes(user);
        }
        return buildRouteTree(context.visibleMenus(), 0L, null);
    }

    @Override
    public AuthAccessSnapshotResponse getAccessSnapshot(AuthUser user) {
        AccessContext context = resolveAccessContext(user);
        if (context.useFallback()) {
            return fallbackRouteService.getAccessSnapshot(user);
        }
        List<String> menuCodeList = context.visibleMenus().stream()
            .map(this::resolveMenuKey)
            .toList();
        LinkedHashSet<String> buttonCodes = new LinkedHashSet<>();
        context.buttonMenus().stream()
            .map(this::resolvePermissionCode)
            .forEach(buttonCodes::add);
        if (user.permissions() != null) {
            buttonCodes.addAll(user.permissions());
        }
        return new AuthAccessSnapshotResponse(
            new AuthAccessUserResponse(
                user.userId(),
                tenantExternalIdService.toExternalTenantId(user.tenantId()),
                user.username(),
                user.nickname(),
                context.roles().stream().map(SystemRoleDO::getRoleCode).toList(),
                menuCodeList,
                List.copyOf(buttonCodes)
            ),
            context.roles().stream()
                .map(role -> new AuthAccessRoleResponse(
                    tenantExternalIdService.toExternalTenantId(role.getTenantId()),
                    String.valueOf(role.getId()),
                    role.getRoleName(),
                    role.getRoleCode()
                ))
                .toList(),
            buildAccessMenuTree(context.visibleMenus(), 0L, null)
        );
    }

    private AccessContext resolveAccessContext(AuthUser user) {
        List<SystemRoleDO> activeRoles = resolveActiveRoles(user);
        if (activeRoles.isEmpty()) {
            boolean shouldFallback = user != null && user.isLocalFallbackUser();
            return new AccessContext(List.of(), List.of(), shouldFallback);
        }
        Set<Long> selectedMenuIds = new LinkedHashSet<>();
        for (SystemRoleDO activeRole : activeRoles) {
            selectedMenuIds.addAll(splitLongCodes(activeRole.getMenuIds()));
        }
        if (selectedMenuIds.isEmpty()) {
            return new AccessContext(activeRoles, List.of(), false);
        }
        LambdaQueryWrapper<MenuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuDO::getStatus, 0);
        queryWrapper.orderByAsc(MenuDO::getSort).orderByAsc(MenuDO::getId);
        List<MenuDO> allMenus = menuMapper.selectList(queryWrapper);
        List<MenuDO> accessibleMenus = expandMenusWithAncestors(allMenus, selectedMenuIds);
        return new AccessContext(activeRoles, accessibleMenus, false);
    }

    private List<SystemRoleDO> resolveActiveRoles(AuthUser user) {
        if (user == null || user.tenantId() == null || user.roles() == null || user.roles().isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleDO::getTenantId, user.tenantId());
        queryWrapper.in(SystemRoleDO::getRoleCode, user.roles());
        List<SystemRoleDO> roles = systemRoleMapper.selectList(queryWrapper).stream()
            .filter(role -> role.getStatus() == null || role.getStatus() == 0)
            .collect(Collectors.toMap(SystemRoleDO::getRoleCode, role -> role, (left, right) -> left, LinkedHashMap::new))
            .values()
            .stream()
            .sorted(Comparator.comparing(SystemRoleDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SystemRoleDO::getRoleCode, Comparator.nullsLast(String::compareTo)))
            .toList();
        if (roles.isEmpty()) {
            return List.of();
        }
        List<SystemRoleDO> orderedRoles = new ArrayList<>();
        for (String roleCode : user.roles()) {
            roles.stream()
                .filter(role -> roleCode.equals(role.getRoleCode()))
                .findFirst()
                .ifPresent(orderedRoles::add);
        }
        return orderedRoles.isEmpty() ? roles : orderedRoles;
    }

    private List<MenuDO> expandMenusWithAncestors(List<MenuDO> allMenus, Collection<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return List.of();
        }
        Map<Long, MenuDO> menuMap = allMenus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        Set<Long> expandedIds = new LinkedHashSet<>();
        for (Long selectedId : selectedIds) {
            MenuDO current = menuMap.get(selectedId);
            while (current != null && expandedIds.add(current.getId())) {
                Long parentId = normalizeParentId(current.getParentId());
                current = parentId.equals(0L) ? null : menuMap.get(parentId);
            }
        }
        return allMenus.stream()
            .filter(menu -> expandedIds.contains(menu.getId()))
            .toList();
    }

    private List<AuthRouteResponse> buildRouteTree(List<MenuDO> menus, Long parentId, Map<Long, MenuDO> cache) {
        Map<Long, MenuDO> menusById = cache == null
            ? menus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item))
            : cache;
        return menus.stream()
            .filter(menu -> isVisibleMenu(menu))
            .filter(menu -> normalizeParentId(menu.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(MenuDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuDO::getId))
            .map(menu -> new AuthRouteResponse(
                FrontendMenuContractMapper.resolvePath(menu, menusById),
                FrontendMenuContractMapper.resolveRouteName(menu),
                FrontendMenuContractMapper.resolveComponent(menu),
                FrontendMenuContractMapper.resolveRedirect(menu),
                buildRouteMeta(menu),
                buildRouteTree(menus, menu.getId(), menusById)
            ))
            .toList();
    }

    private List<AuthAccessMenuTreeItemResponse> buildAccessMenuTree(List<MenuDO> menus, Long parentId, Map<Long, MenuDO> cache) {
        Map<Long, MenuDO> menusById = cache == null
            ? menus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item))
            : cache;
        return menus.stream()
            .filter(menu -> isVisibleMenu(menu))
            .filter(menu -> normalizeParentId(menu.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(MenuDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuDO::getId))
            .map(menu -> {
                List<AuthAccessMenuTreeItemResponse> children = buildAccessMenuTree(menus, menu.getId(), menusById);
                return new AuthAccessMenuTreeItemResponse(
                    normalizeParentId(menu.getParentId()),
                    menu.getId(),
                    menu.getMenuName(),
                    FrontendMenuContractMapper.resolveRouteName(menu),
                    resolveMenuType(menu),
                    FrontendMenuContractMapper.resolvePath(menu, menusById),
                    resolveMenuKey(menu),
                    resolvePermissionCode(menu),
                    defaultString(menu.getIcon(), ""),
                    defaultString(menu.getLayout(), ""),
                    menu.getVisible() == null || menu.getVisible() == 1,
                    menu.getStatus() == null || menu.getStatus() == 0,
                    FrontendMenuContractMapper.resolveComponent(menu),
                    FrontendMenuContractMapper.resolveRedirect(menu),
                    parseMeta(menu.getMeta()),
                    menu.getSort() == null ? 0 : menu.getSort(),
                    toIsoInstant(menu.getCreateTime()),
                    toIsoInstant(menu.getUpdateTime()),
                    children.isEmpty() ? null : children
                );
            })
            .toList();
    }

    private Map<String, Object> buildRouteMeta(MenuDO menu) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", menu.getMenuName());
        if (StringUtils.hasText(menu.getIcon())) {
            meta.put("icon", menu.getIcon().trim());
        }
        meta.put("hidden", menu.getVisible() != null && menu.getVisible() != 1);
        meta.put("keepAlive", menu.getKeepAlive() != null && menu.getKeepAlive() == 1);
        meta.put("order", menu.getSort() == null ? 0 : menu.getSort());
        meta.put("menuKey", resolveMenuKey(menu));
        meta.put("permissionCode", resolvePermissionCode(menu));
        meta.put("type", resolveMenuType(menu));
        if (StringUtils.hasText(menu.getLayout())) {
            meta.put("layout", menu.getLayout().trim());
        }
        Map<String, Object> persistedMeta = parseMeta(menu.getMeta());
        if (persistedMeta != null && !persistedMeta.isEmpty()) {
            meta.putAll(persistedMeta);
        }
        return meta;
    }

    private Map<String, Object> parseMeta(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse menu meta", exception);
        }
    }

    private boolean isVisibleMenu(MenuDO menu) {
        return menu != null && resolveMenuType(menu) != 3;
    }

    private Integer resolveMenuType(MenuDO menu) {
        if (menu == null || !StringUtils.hasText(menu.getMenuType())) {
            return 2;
        }
        return switch (menu.getMenuType().trim().toUpperCase()) {
            case "DIRECTORY" -> 1;
            case "BUTTON" -> 3;
            default -> 2;
        };
    }

    private String resolveMenuKey(MenuDO menu) {
        if (menu == null) {
            return null;
        }
        if (StringUtils.hasText(menu.getMenuKey())) {
            return menu.getMenuKey().trim();
        }
        if (StringUtils.hasText(menu.getPermissionCode())) {
            return menu.getPermissionCode().trim();
        }
        if (StringUtils.hasText(menu.getRouteName())) {
            return menu.getRouteName().trim();
        }
        return "menu:" + menu.getId();
    }

    private String resolvePermissionCode(MenuDO menu) {
        if (menu != null && StringUtils.hasText(menu.getPermissionCode())) {
            return menu.getPermissionCode().trim();
        }
        return resolveMenuKey(menu);
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private List<Long> splitLongCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String token : values.split(",")) {
            String trimmed = token == null ? null : token.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }
            result.add(Long.valueOf(trimmed));
        }
        return result;
    }

    private String toIsoInstant(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC).toInstant().toString();
    }

    private record AccessContext(
        List<SystemRoleDO> roles,
        List<MenuDO> menus,
        boolean useFallback
    ) {
        List<MenuDO> visibleMenus() {
            return menus.stream().filter(menu -> {
                String menuType = menu.getMenuType();
                return !StringUtils.hasText(menuType) || !"BUTTON".equalsIgnoreCase(menuType.trim());
            }).toList();
        }

        List<MenuDO> buttonMenus() {
            return menus.stream().filter(menu -> StringUtils.hasText(menu.getMenuType())
                && "BUTTON".equalsIgnoreCase(menu.getMenuType().trim())).toList();
        }
    }
}
