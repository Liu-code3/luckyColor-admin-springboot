package com.luckycolor.admin.modules.iam.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.modules.iam.auth.config.AuthAccessProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.impl.AuthAccessRouteServiceImpl;
import com.luckycolor.admin.modules.iam.auth.service.impl.PersistenceAuthAccessRouteServiceImpl;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PersistenceAuthAccessRouteServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldBuildRoutesAndAccessSnapshotFromDatabaseMenus() {
        SystemRoleMapper systemRoleMapper = Mockito.mock(SystemRoleMapper.class);
        MenuMapper menuMapper = Mockito.mock(MenuMapper.class);
        AuthAccessRouteServiceImpl fallback = new AuthAccessRouteServiceImpl(buildFallbackProperties());
        PersistenceAuthAccessRouteServiceImpl service = new PersistenceAuthAccessRouteServiceImpl(
            systemRoleMapper,
            menuMapper,
            fallback,
            objectMapper
        );

        SystemRoleDO role = role(1L, 1L, "ROLE_SUPER_ADMIN", "Super Admin");
        role.setMenuIds("10,11,21");

        MenuDO system = menu(10L, 0L, "System", "DIRECTORY", "System", "/system", "Layout");
        system.setMenuKey("main_system");
        system.setRedirect("/system/users");
        system.setMeta("{\"badge\":\"ops\"}");

        MenuDO users = menu(11L, 10L, "System User", "MENU", "SystemUser", "users", "system/user/index");
        users.setMenuKey("main_system_users");
        users.setPermissionCode("system:user:query");
        users.setMeta("{\"keepAlive\":true,\"title\":\"System User\"}");

        MenuDO createButton = menu(21L, 11L, "Create User", "BUTTON", "SystemUserCreate", "create", "");
        createButton.setPermissionCode("system:user:create");

        when(systemRoleMapper.selectList(any())).thenReturn(List.of(role));
        when(menuMapper.selectList(any())).thenReturn(List.of(system, users, createButton));

        AuthUser user = new AuthUser(
            1L,
            "admin",
            "encoded",
            1L,
            "System Admin",
            0,
            List.of("ROLE_SUPER_ADMIN"),
            List.of("system:user:reset-password")
        );

        List<AuthRouteResponse> routes = service.getAccessibleRoutes(user);
        AuthAccessSnapshotResponse snapshot = service.getAccessSnapshot(user);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).path()).isEqualTo("/system");
        assertThat(routes.get(0).redirect()).isEqualTo("/system/users");
        assertThat(routes.get(0).meta()).containsEntry("menuKey", "main_system");
        assertThat(routes.get(0).children()).hasSize(1);
        assertThat(routes.get(0).children().get(0).path()).isEqualTo("/system/users");
        assertThat(routes.get(0).children().get(0).meta()).containsEntry("permissionCode", "system:user:query");
        assertThat(routes.get(0).children().get(0).meta()).containsEntry("keepAlive", true);

        assertThat(snapshot.user().roleCodes()).containsExactly("ROLE_SUPER_ADMIN");
        assertThat(snapshot.user().menuCodeList()).containsExactly("main_system", "system:user:query");
        assertThat(snapshot.user().buttonCodeList()).containsExactly("system:user:create", "system:user:reset-password");
        assertThat(snapshot.roles()).extracting(AuthAccessSnapshotResponse.AuthAccessRoleResponse::name)
            .containsExactly("Super Admin");
        assertThat(snapshot.menuTree()).hasSize(1);
        assertThat(snapshot.menuTree().get(0).meta()).containsEntry("badge", "ops");
        assertThat(snapshot.menuTree().get(0).children()).hasSize(1);
        assertThat(snapshot.menuTree().get(0).children().get(0).meta()).containsEntry("keepAlive", true);
        assertThat(snapshot.menuTree().get(0).children().get(0).createdAt()).isEqualTo("2026-04-03T10:00:00Z");
    }

    @Test
    void shouldFallbackToPropertyRoutesWhenDatabaseAccessContextIsMissing() {
        SystemRoleMapper systemRoleMapper = Mockito.mock(SystemRoleMapper.class);
        MenuMapper menuMapper = Mockito.mock(MenuMapper.class);
        AuthAccessRouteServiceImpl fallback = new AuthAccessRouteServiceImpl(buildFallbackProperties());
        PersistenceAuthAccessRouteServiceImpl service = new PersistenceAuthAccessRouteServiceImpl(
            systemRoleMapper,
            menuMapper,
            fallback,
            objectMapper
        );

        when(systemRoleMapper.selectList(any())).thenReturn(List.of());

        AuthUser user = new AuthUser(
            99L,
            "local-admin",
            "local123",
            9L,
            "Local Admin",
            0,
            List.of("ROLE_LOCAL_ADMIN"),
            List.of("local:debug")
        );

        List<AuthRouteResponse> routes = service.getAccessibleRoutes(user);

        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).path()).isEqualTo("/fallback");
        verify(menuMapper, never()).selectList(any());
    }

    @Test
    void shouldReturnEmptyRoutesWhenUserHasNoAssignedRoles() {
        SystemRoleMapper systemRoleMapper = Mockito.mock(SystemRoleMapper.class);
        MenuMapper menuMapper = Mockito.mock(MenuMapper.class);
        AuthAccessRouteServiceImpl fallback = new AuthAccessRouteServiceImpl(buildFallbackProperties());
        PersistenceAuthAccessRouteServiceImpl service = new PersistenceAuthAccessRouteServiceImpl(
            systemRoleMapper,
            menuMapper,
            fallback,
            objectMapper
        );

        AuthUser user = new AuthUser(
            2L,
            "member",
            "encoded",
            1L,
            "Member",
            0,
            List.of(),
            List.of()
        );

        List<AuthRouteResponse> routes = service.getAccessibleRoutes(user);
        AuthAccessSnapshotResponse snapshot = service.getAccessSnapshot(user);

        assertThat(routes).isEmpty();
        assertThat(snapshot.user().menuCodeList()).isEmpty();
        assertThat(snapshot.user().buttonCodeList()).isEmpty();
        assertThat(snapshot.menuTree()).isEmpty();
        verify(systemRoleMapper, never()).selectList(any());
        verify(menuMapper, never()).selectList(any());
    }

    private MenuDO menu(Long id, Long parentId, String title, String type, String name, String path, String component) {
        MenuDO menu = new MenuDO();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(title);
        menu.setMenuType(type);
        menu.setRouteName(name);
        menu.setRoutePath(path);
        menu.setComponent(component);
        menu.setLayout("default");
        menu.setSort(1);
        menu.setVisible(1);
        menu.setKeepAlive(1);
        menu.setStatus(0);
        menu.setCreateTime(LocalDateTime.of(2026, 4, 3, 10, 0));
        menu.setUpdateTime(LocalDateTime.of(2026, 4, 3, 10, 30));
        return menu;
    }

    private SystemRoleDO role(Long id, Long tenantId, String code, String name) {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(id);
        role.setTenantId(tenantId);
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setSort(1);
        role.setStatus(0);
        return role;
    }

    private AuthAccessProperties buildFallbackProperties() {
        AuthAccessProperties properties = new AuthAccessProperties();
        AuthAccessProperties.Route fallback = new AuthAccessProperties.Route();
        fallback.setCode("fallback");
        fallback.setName("Fallback");
        fallback.setPath("/fallback");
        fallback.setComponent("fallback/index");
        fallback.setRoles(List.of("ROLE_LOCAL_ADMIN"));
        properties.setRoutes(List.of(fallback));
        return properties;
    }
}
