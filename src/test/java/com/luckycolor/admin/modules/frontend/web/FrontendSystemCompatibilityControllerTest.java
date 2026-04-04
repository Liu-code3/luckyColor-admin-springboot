package com.luckycolor.admin.modules.frontend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.page.PageQuery;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import com.luckycolor.admin.modules.system.department.dataobject.SystemDepartmentDO;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.role.service.SystemRoleService;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleAuthorityRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserAssignRolesRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FrontendSystemCompatibilityControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private SystemUserService systemUserService;
    private SystemUserMapper systemUserMapper;
    private SystemRoleService systemRoleService;
    private SystemRoleMapper systemRoleMapper;
    private SystemDepartmentService systemDepartmentService;
    private SystemDepartmentMapper systemDepartmentMapper;
    private MenuService menuService;
    private MenuMapper menuMapper;
    private DataScopeConditionBuilder dataScopeConditionBuilder;
    private FrontendSystemCompatibilityController controller;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @BeforeEach
    void setUp() {
        systemUserService = Mockito.mock(SystemUserService.class);
        systemUserMapper = Mockito.mock(SystemUserMapper.class);
        systemRoleService = Mockito.mock(SystemRoleService.class);
        systemRoleMapper = Mockito.mock(SystemRoleMapper.class);
        systemDepartmentService = Mockito.mock(SystemDepartmentService.class);
        systemDepartmentMapper = Mockito.mock(SystemDepartmentMapper.class);
        menuService = Mockito.mock(MenuService.class);
        menuMapper = Mockito.mock(MenuMapper.class);
        dataScopeConditionBuilder = Mockito.mock(DataScopeConditionBuilder.class);
        controller = new FrontendSystemCompatibilityController(
            systemUserService,
            systemUserMapper,
            systemRoleService,
            systemRoleMapper,
            systemDepartmentService,
            systemDepartmentMapper,
            menuService,
            menuMapper,
            dataScopeConditionBuilder,
            objectMapper
        );
    }

    @Test
    void shouldExposeFrontendUserPagingContract() throws Exception {
        SystemUserDO user = new SystemUserDO();
        user.setId(1L);
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setNickname("System Admin");
        user.setDepartmentId(100L);
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.of(2026, 4, 2, 10, 0));
        user.setUpdateTime(LocalDateTime.of(2026, 4, 2, 10, 30));

        SystemDepartmentDO department = new SystemDepartmentDO();
        department.setId(100L);
        department.setDepartmentName("Headquarters");

        when(systemUserMapper.selectPageResult(any(PageQuery.class), any())).thenReturn(PageResult.of(List.of(user), 1));
        when(systemDepartmentMapper.selectBatchIds(List.of(100L))).thenReturn(List.of(department));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/users").param("page", "1").param("size", "10").param("keyword", "admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.current").value(1))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].id").value("1"))
            .andExpect(jsonPath("$.data.records[0].departmentName").value("Headquarters"))
            .andExpect(jsonPath("$.data.records[0].status").value(true));
    }

    @Test
    void shouldTranslateRoleIdsWhenAssigningUserRoles() {
        SystemUserDO before = new SystemUserDO();
        before.setId(1L);
        before.setUsername("alice");
        before.setNickname("Alice");

        SystemUserDO after = new SystemUserDO();
        after.setId(1L);
        after.setUsername("alice");
        after.setNickname("Alice");
        after.setRoleCodes("ROLE_ADMIN,ROLE_EDITOR");

        SystemRoleDO adminRole = role(11L, "ROLE_ADMIN", "Admin");
        SystemRoleDO editorRole = role(12L, "ROLE_EDITOR", "Editor");

        when(systemUserMapper.selectById(1L)).thenReturn(before, after);
        when(systemRoleMapper.selectBatchIds(List.of(11L, 12L))).thenReturn(List.of(adminRole, editorRole));

        FrontendSystemCompatibilityController.FrontendRoleIdsRequest request =
            new FrontendSystemCompatibilityController.FrontendRoleIdsRequest();
        request.setRoleIds(List.of("11", "12"));

        FrontendSystemCompatibilityController.FrontendUserRoleAssignment response =
            controller.assignUserRoles(1L, request).data();

        ArgumentCaptor<SystemUserAssignRolesRequest> captor = ArgumentCaptor.forClass(SystemUserAssignRolesRequest.class);
        verify(systemUserService).assignRoles(eq(1L), captor.capture());
        assertThat(captor.getValue().getRoleCodes()).containsExactly("ROLE_ADMIN", "ROLE_EDITOR");
        assertThat(response.roleIds()).containsExactly("11", "12");
        assertThat(response.roles()).hasSize(2);
    }

    @Test
    void shouldMapRoleDataScopeToFrontendShape() {
        SystemRoleDO role = role(9L, "tenant_admin", "Tenant Admin");
        when(systemRoleMapper.selectById(9L)).thenReturn(role);
        when(systemRoleService.getRoleAuthority(9L)).thenReturn(
            new SystemRoleAuthorityResponse(9L, 1L, "tenant_admin", "Tenant Admin", List.of(), List.of(), "DEPARTMENT_AND_CHILDREN", null, List.of(100L, 101L))
        );

        FrontendSystemCompatibilityController.FrontendRoleDataScopeAssignment response =
            controller.getRoleDataScope(9L).data();

        assertThat(response.dataScopeType()).isEqualTo("DEPT_AND_CHILD");
        assertThat(response.customDeptIds()).containsExactly(100L, 101L);
    }

    @Test
    void shouldMergeMenuPatchIntoNativeMenuSaveRequest() {
        MenuDO current = new MenuDO();
        current.setId(18L);
        current.setParentId(10L);
        current.setMenuName("System User");
        current.setMenuType("MENU");
        current.setRouteName("SystemUser");
        current.setRoutePath("users");
        current.setComponent("system/user/index");
        current.setPermissionCode("system:user:query");
        current.setRoleCodes("ROLE_SUPER_ADMIN");
        current.setIcon("user");
        current.setSort(1);
        current.setVisible(1);
        current.setKeepAlive(1);
        current.setAlwaysShow(0);
        current.setStatus(0);

        MenuDO updated = new MenuDO();
        updated.setId(18L);
        updated.setParentId(10L);
        updated.setMenuName("System User");
        updated.setMenuType("MENU");
        updated.setRouteName("SystemUser");
        updated.setRoutePath("users");
        updated.setComponent("system/user/index");
        updated.setPermissionCode("system:user:query");
        updated.setIcon("user");
        updated.setSort(1);
        updated.setVisible(0);
        updated.setKeepAlive(1);
        updated.setAlwaysShow(0);
        updated.setStatus(1);

        when(menuMapper.selectById(18L)).thenReturn(current, updated);
        when(menuMapper.selectList(any())).thenReturn(List.of(updated));

        FrontendSystemCompatibilityController.FrontendMenuPatchRequest request =
            new FrontendSystemCompatibilityController.FrontendMenuPatchRequest();
        request.setIsVisible(false);
        request.setStatus(false);

        FrontendSystemCompatibilityController.FrontendMenuRecord response =
            controller.updateMenu(18L, request).data();

        ArgumentCaptor<com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest> captor =
            ArgumentCaptor.forClass(com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest.class);
        verify(menuService).updateMenu(eq(18L), captor.capture());
        assertThat(captor.getValue().getVisible()).isEqualTo(0);
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(response.isVisible()).isFalse();
        assertThat(response.status()).isFalse();
    }

    @Test
    void shouldPreferExplicitPermissionCodeWhenCreatingMenu() {
        MenuDO created = new MenuDO();
        created.setId(21L);
        created.setParentId(0L);
        created.setMenuName("System User");
        created.setMenuType("MENU");
        created.setRouteName("SystemUser");
        created.setRoutePath("/system/users");
        created.setComponent("system/user/index");
        created.setPermissionCode("system:user:read");
        created.setSort(1);
        created.setVisible(1);
        created.setKeepAlive(1);
        created.setStatus(0);

        when(menuService.createMenu(any())).thenReturn(21L);
        when(menuMapper.selectById(21L)).thenReturn(created);
        when(menuMapper.selectList(any())).thenReturn(List.of(created));

        FrontendSystemCompatibilityController.FrontendMenuUpsertRequest request =
            new FrontendSystemCompatibilityController.FrontendMenuUpsertRequest();
        request.setParentId(0L);
        request.setTitle("System User");
        request.setName("SystemUser");
        request.setType(2);
        request.setPath("/system/users");
        request.setMenuKey("main_system_users");
        request.setPermissionCode("system:user:read");
        request.setIsVisible(true);
        request.setStatus(true);
        request.setComponent("system/user/index");

        FrontendSystemCompatibilityController.FrontendMenuRecord response =
            controller.createMenu(request).data();

        ArgumentCaptor<MenuSaveRequest> captor = ArgumentCaptor.forClass(MenuSaveRequest.class);
        verify(menuService).createMenu(captor.capture());
        assertThat(captor.getValue().getPermissionCode()).isEqualTo("system:user:read");
        assertThat(response.permissionCode()).isEqualTo("system:user:read");
    }

    @Test
    void shouldFallbackToMenuKeyWhenPatchPermissionCodeIsBlank() {
        MenuDO current = new MenuDO();
        current.setId(18L);
        current.setParentId(10L);
        current.setMenuName("System User");
        current.setMenuType("MENU");
        current.setRouteName("SystemUser");
        current.setRoutePath("users");
        current.setComponent("system/user/index");
        current.setPermissionCode("system:user:query");
        current.setSort(1);
        current.setVisible(1);
        current.setKeepAlive(1);
        current.setAlwaysShow(0);
        current.setStatus(0);

        MenuDO updated = new MenuDO();
        updated.setId(18L);
        updated.setParentId(10L);
        updated.setMenuName("System User");
        updated.setMenuType("MENU");
        updated.setRouteName("SystemUser");
        updated.setRoutePath("users");
        updated.setComponent("system/user/index");
        updated.setPermissionCode("main_system_users");
        updated.setSort(1);
        updated.setVisible(1);
        updated.setKeepAlive(1);
        updated.setAlwaysShow(0);
        updated.setStatus(0);

        when(menuMapper.selectById(18L)).thenReturn(current, updated);
        when(menuMapper.selectList(any())).thenReturn(List.of(updated));

        FrontendSystemCompatibilityController.FrontendMenuPatchRequest request =
            new FrontendSystemCompatibilityController.FrontendMenuPatchRequest();
        request.setMenuKey("main_system_users");
        request.setPermissionCode(" ");

        FrontendSystemCompatibilityController.FrontendMenuRecord response =
            controller.updateMenu(18L, request).data();

        ArgumentCaptor<MenuSaveRequest> captor = ArgumentCaptor.forClass(MenuSaveRequest.class);
        verify(menuService).updateMenu(eq(18L), captor.capture());
        assertThat(captor.getValue().getPermissionCode()).isEqualTo("main_system_users");
        assertThat(response.permissionCode()).isEqualTo("main_system_users");
    }

    @Test
    void shouldExposeMenuStatusPatchContract() {
        MenuDO current = new MenuDO();
        current.setId(18L);
        current.setParentId(10L);
        current.setMenuName("System User");
        current.setMenuType("MENU");
        current.setRouteName("SystemUser");
        current.setRoutePath("users");
        current.setComponent("system/user/index");
        current.setPermissionCode("system:user:query");
        current.setSort(1);
        current.setVisible(1);
        current.setKeepAlive(1);
        current.setStatus(0);

        MenuDO updated = new MenuDO();
        updated.setId(18L);
        updated.setParentId(10L);
        updated.setMenuName("System User");
        updated.setMenuType("MENU");
        updated.setRouteName("SystemUser");
        updated.setRoutePath("users");
        updated.setComponent("system/user/index");
        updated.setPermissionCode("system:user:query");
        updated.setSort(1);
        updated.setVisible(1);
        updated.setKeepAlive(1);
        updated.setStatus(1);

        when(menuMapper.selectById(18L)).thenReturn(current, updated);
        when(menuMapper.selectList(any())).thenReturn(List.of(updated));

        FrontendSystemCompatibilityController.FrontendMenuStatusRequest request =
            new FrontendSystemCompatibilityController.FrontendMenuStatusRequest();
        request.setStatus(false);

        FrontendSystemCompatibilityController.FrontendMenuRecord response =
            controller.updateMenuStatus(18L, request).data();

        ArgumentCaptor<MenuStatusRequest> captor = ArgumentCaptor.forClass(MenuStatusRequest.class);
        verify(menuService).updateMenuStatus(eq(18L), captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(response.status()).isFalse();
        assertThat(response.permissionCode()).isEqualTo("system:user:query");
    }

    @Test
    void shouldExposeNativeMenuTreeContract() throws Exception {
        MenuDO dashboard = new MenuDO();
        dashboard.setId(1L);
        dashboard.setParentId(0L);
        dashboard.setMenuName("Dashboard");
        dashboard.setMenuType("MENU");
        dashboard.setRouteName("Dashboard");
        dashboard.setRoutePath("/dashboard");
        dashboard.setComponent("dashboard/index");
        dashboard.setPermissionCode("dashboard:query");
        dashboard.setSort(1);
        dashboard.setVisible(1);
        dashboard.setKeepAlive(1);
        dashboard.setStatus(0);

        MenuDO systemRoot = new MenuDO();
        systemRoot.setId(10L);
        systemRoot.setParentId(0L);
        systemRoot.setMenuName("System");
        systemRoot.setMenuType("DIRECTORY");
        systemRoot.setRouteName("System");
        systemRoot.setRoutePath("/system");
        systemRoot.setComponent("Layout");
        systemRoot.setSort(10);
        systemRoot.setVisible(1);
        systemRoot.setKeepAlive(0);
        systemRoot.setStatus(0);

        MenuDO systemUser = new MenuDO();
        systemUser.setId(11L);
        systemUser.setParentId(10L);
        systemUser.setMenuName("System User");
        systemUser.setMenuType("MENU");
        systemUser.setRouteName("SystemUser");
        systemUser.setRoutePath("users");
        systemUser.setComponent("system/user/index");
        systemUser.setPermissionCode("system:user:query");
        systemUser.setSort(1);
        systemUser.setVisible(1);
        systemUser.setKeepAlive(1);
        systemUser.setStatus(0);

        when(menuMapper.selectList(any())).thenReturn(List.of(dashboard, systemRoot, systemUser));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/menus/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].path").value("/dashboard"))
            .andExpect(jsonPath("$.data[0].component").value("index/index"))
            .andExpect(jsonPath("$.data[0].permissionCode").value("dashboard:query"))
            .andExpect(jsonPath("$.data[1].path").value("/system"))
            .andExpect(jsonPath("$.data[1].component").value("sys/index"))
            .andExpect(jsonPath("$.data[1].children[0].path").value("/system/users"))
            .andExpect(jsonPath("$.data[1].children[0].component").value("sys/user"))
            .andExpect(jsonPath("$.data[1].children[0].permissionCode").value("system:user:query"));
    }

    @Test
    void shouldExposePermissionCodeInMenuPagingContract() throws Exception {
        MenuDO dashboard = new MenuDO();
        dashboard.setId(1L);
        dashboard.setParentId(0L);
        dashboard.setMenuName("Dashboard");
        dashboard.setMenuType("MENU");
        dashboard.setRouteName("Dashboard");
        dashboard.setRoutePath("/dashboard");
        dashboard.setComponent("dashboard/index");
        dashboard.setPermissionCode("dashboard:query");
        dashboard.setSort(1);
        dashboard.setVisible(1);
        dashboard.setKeepAlive(1);
        dashboard.setStatus(0);

        when(menuMapper.selectList(any())).thenReturn(List.of(dashboard));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/menus").param("page", "1").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].path").value("/dashboard"))
            .andExpect(jsonPath("$.data.records[0].key").value("dashboard:query"))
            .andExpect(jsonPath("$.data.records[0].permissionCode").value("dashboard:query"));
    }

    @Test
    void shouldExposePersistedMenuContractFields() {
        MenuDO systemRoot = new MenuDO();
        systemRoot.setId(10L);
        systemRoot.setParentId(0L);
        systemRoot.setMenuName("System");
        systemRoot.setMenuType("DIRECTORY");
        systemRoot.setRouteName("System");
        systemRoot.setRoutePath("/system");
        systemRoot.setMenuKey("main_system");
        systemRoot.setComponent("Layout");
        systemRoot.setRedirect("/system/users");
        systemRoot.setMeta("{\"badge\":\"ops\"}");
        systemRoot.setLayout("default");
        systemRoot.setSort(1);
        systemRoot.setVisible(1);
        systemRoot.setKeepAlive(0);
        systemRoot.setStatus(0);

        MenuDO systemUser = new MenuDO();
        systemUser.setId(11L);
        systemUser.setParentId(10L);
        systemUser.setMenuName("System User");
        systemUser.setMenuType("MENU");
        systemUser.setRouteName("SystemUser");
        systemUser.setRoutePath("users");
        systemUser.setMenuKey("main_system_users");
        systemUser.setComponent("system/user/index");
        systemUser.setRedirect("/system/users/list");
        systemUser.setMeta("{\"badge\":\"beta\",\"keepAlive\":true}");
        systemUser.setPermissionCode("system:user:query");
        systemUser.setLayout("default");
        systemUser.setSort(1);
        systemUser.setVisible(1);
        systemUser.setKeepAlive(1);
        systemUser.setStatus(0);

        when(menuMapper.selectById(11L)).thenReturn(systemUser);
        when(menuMapper.selectList(any())).thenReturn(List.of(systemRoot, systemUser));

        FrontendSystemCompatibilityController.FrontendMenuRecord response = controller.getMenu(11L).data();

        assertThat(response.key()).isEqualTo("main_system_users");
        assertThat(response.permissionCode()).isEqualTo("system:user:query");
        assertThat(response.layout()).isEqualTo("default");
        assertThat(response.redirect()).isEqualTo("/system/users/list");
        assertThat(response.meta()).containsEntry("badge", "beta");
        assertThat(response.meta()).containsEntry("title", "System User");
        assertThat(response.meta()).containsEntry("keepAlive", true);
        assertThat(response.meta()).containsEntry("hidden", false);
    }

    @Test
    void shouldFilterTenantMenuTreeAndKeepAncestors() {
        TenantContextHolder.setTenantId(1L);

        MenuDO systemRoot = new MenuDO();
        systemRoot.setId(10L);
        systemRoot.setParentId(0L);
        systemRoot.setMenuName("System");
        systemRoot.setMenuType("DIRECTORY");
        systemRoot.setRouteName("System");
        systemRoot.setRoutePath("/system");
        systemRoot.setComponent("Layout");
        systemRoot.setSort(1);
        systemRoot.setVisible(1);
        systemRoot.setStatus(0);

        MenuDO systemUser = new MenuDO();
        systemUser.setId(11L);
        systemUser.setParentId(10L);
        systemUser.setMenuName("System User");
        systemUser.setMenuType("MENU");
        systemUser.setRouteName("SystemUser");
        systemUser.setRoutePath("users");
        systemUser.setComponent("system/user/index");
        systemUser.setPermissionCode("system:user:query");
        systemUser.setSort(1);
        systemUser.setVisible(1);
        systemUser.setStatus(0);

        MenuDO tenantPackage = new MenuDO();
        tenantPackage.setId(20L);
        tenantPackage.setParentId(0L);
        tenantPackage.setMenuName("Tenant Package");
        tenantPackage.setMenuType("MENU");
        tenantPackage.setRouteName("TenantPackage");
        tenantPackage.setRoutePath("/tenant/package");
        tenantPackage.setComponent("tenant/package/index");
        tenantPackage.setPermissionCode("tenant:package:query");
        tenantPackage.setSort(2);
        tenantPackage.setVisible(1);
        tenantPackage.setStatus(0);

        SystemRoleDO tenantRole = role(1L, "tenant_admin", "Tenant Admin");
        tenantRole.setTenantId(1L);
        tenantRole.setMenuIds("11");

        SystemRoleDO otherTenantRole = role(2L, "other_tenant_admin", "Other Tenant Admin");
        otherTenantRole.setTenantId(2L);
        otherTenantRole.setMenuIds("20");

        when(menuMapper.selectList(any())).thenReturn(List.of(systemRoot, systemUser, tenantPackage));
        when(systemRoleMapper.selectList(any())).thenReturn(List.of(tenantRole));

        List<FrontendSystemCompatibilityController.FrontendMenuRecord> response =
            controller.menuTree("tenant", null).data();

        assertThat(response).extracting(FrontendSystemCompatibilityController.FrontendMenuRecord::id)
            .containsExactly(10L);
        assertThat(response.get(0).children()).extracting(FrontendSystemCompatibilityController.FrontendMenuRecord::id)
            .containsExactly(11L);
    }

    @Test
    void shouldFilterRoleScopedMenuTreeAndKeepAncestors() {
        TenantContextHolder.setTenantId(1L);

        MenuDO systemRoot = new MenuDO();
        systemRoot.setId(10L);
        systemRoot.setParentId(0L);
        systemRoot.setMenuName("System");
        systemRoot.setMenuType("DIRECTORY");
        systemRoot.setRouteName("System");
        systemRoot.setRoutePath("/system");
        systemRoot.setComponent("Layout");
        systemRoot.setSort(1);
        systemRoot.setVisible(1);
        systemRoot.setStatus(0);

        MenuDO systemUser = new MenuDO();
        systemUser.setId(11L);
        systemUser.setParentId(10L);
        systemUser.setMenuName("System User");
        systemUser.setMenuType("MENU");
        systemUser.setRouteName("SystemUser");
        systemUser.setRoutePath("users");
        systemUser.setComponent("system/user/index");
        systemUser.setPermissionCode("system:user:query");
        systemUser.setSort(1);
        systemUser.setVisible(1);
        systemUser.setStatus(0);

        MenuDO tenantPackage = new MenuDO();
        tenantPackage.setId(20L);
        tenantPackage.setParentId(0L);
        tenantPackage.setMenuName("Tenant Package");
        tenantPackage.setMenuType("MENU");
        tenantPackage.setRouteName("TenantPackage");
        tenantPackage.setRoutePath("/tenant/package");
        tenantPackage.setComponent("tenant/package/index");
        tenantPackage.setPermissionCode("tenant:package:query");
        tenantPackage.setSort(2);
        tenantPackage.setVisible(1);
        tenantPackage.setStatus(0);

        SystemRoleDO role = role(9L, "tenant_admin", "Tenant Admin");
        role.setTenantId(1L);
        role.setMenuIds("11");

        when(menuMapper.selectList(any())).thenReturn(List.of(systemRoot, systemUser, tenantPackage));
        when(systemRoleMapper.selectById(9L)).thenReturn(role);

        List<FrontendSystemCompatibilityController.FrontendMenuRecord> response =
            controller.menuTree(null, 9L).data();

        assertThat(response).extracting(FrontendSystemCompatibilityController.FrontendMenuRecord::id)
            .containsExactly(10L);
        assertThat(response.get(0).children()).extracting(FrontendSystemCompatibilityController.FrontendMenuRecord::id)
            .containsExactly(11L);
    }

    @Test
    void shouldSyncMenusAndReturnLatestTree() {
        MenuDO dashboard = new MenuDO();
        dashboard.setId(1L);
        dashboard.setParentId(0L);
        dashboard.setMenuName("Dashboard");
        dashboard.setMenuType("MENU");
        dashboard.setRouteName("Dashboard");
        dashboard.setRoutePath("/dashboard");
        dashboard.setComponent("dashboard/index");
        dashboard.setPermissionCode("dashboard:query");
        dashboard.setSort(1);
        dashboard.setVisible(1);
        dashboard.setKeepAlive(1);
        dashboard.setStatus(0);

        MenuDO systemRoot = new MenuDO();
        systemRoot.setId(10L);
        systemRoot.setParentId(0L);
        systemRoot.setMenuName("System");
        systemRoot.setMenuType("DIRECTORY");
        systemRoot.setRouteName("System");
        systemRoot.setRoutePath("/system");
        systemRoot.setComponent("Layout");
        systemRoot.setSort(10);
        systemRoot.setVisible(1);
        systemRoot.setKeepAlive(0);
        systemRoot.setStatus(0);

        MenuDO systemUser = new MenuDO();
        systemUser.setId(11L);
        systemUser.setParentId(10L);
        systemUser.setMenuName("System User");
        systemUser.setMenuType("MENU");
        systemUser.setRouteName("SystemUser");
        systemUser.setRoutePath("users");
        systemUser.setComponent("system/user/index");
        systemUser.setPermissionCode("system:user:query");
        systemUser.setSort(5);
        systemUser.setVisible(1);
        systemUser.setKeepAlive(1);
        systemUser.setStatus(0);

        MenuDO syncedDashboard = cloneMenu(dashboard);
        syncedDashboard.setSort(3);

        MenuDO syncedSystemRoot = cloneMenu(systemRoot);

        MenuDO syncedSystemUser = cloneMenu(systemUser);
        syncedSystemUser.setParentId(0L);
        syncedSystemUser.setSort(2);

        when(menuMapper.selectList(any())).thenReturn(
            List.of(dashboard, systemRoot, systemUser),
            List.of(syncedDashboard, syncedSystemRoot, syncedSystemUser)
        );

        FrontendSystemCompatibilityController.FrontendMenuSyncRequest request =
            new FrontendSystemCompatibilityController.FrontendMenuSyncRequest();
        FrontendSystemCompatibilityController.FrontendMenuSyncItemRequest first =
            new FrontendSystemCompatibilityController.FrontendMenuSyncItemRequest();
        first.setId(11L);
        first.setParentId(0L);
        first.setSort(2);
        FrontendSystemCompatibilityController.FrontendMenuSyncItemRequest second =
            new FrontendSystemCompatibilityController.FrontendMenuSyncItemRequest();
        second.setId(1L);
        second.setParentId(0L);
        second.setSort(3);
        request.setMenus(List.of(first, second));

        List<FrontendSystemCompatibilityController.FrontendMenuRecord> response =
            controller.syncMenus(request).data();

        verify(menuMapper, times(2)).updateById(any(MenuDO.class));
        assertThat(response).extracting(FrontendSystemCompatibilityController.FrontendMenuRecord::id)
            .containsExactly(11L, 1L, 10L);
        assertThat(response.get(0).pid()).isEqualTo(0L);
        assertThat(response.get(0).path()).isEqualTo("/users");
        assertThat(response.get(0).sort()).isEqualTo(2);
    }

    private MenuDO cloneMenu(MenuDO source) {
        MenuDO target = new MenuDO();
        target.setId(source.getId());
        target.setParentId(source.getParentId());
        target.setMenuName(source.getMenuName());
        target.setMenuType(source.getMenuType());
        target.setRouteName(source.getRouteName());
        target.setRoutePath(source.getRoutePath());
        target.setMenuKey(source.getMenuKey());
        target.setComponent(source.getComponent());
        target.setRedirect(source.getRedirect());
        target.setMeta(source.getMeta());
        target.setPermissionCode(source.getPermissionCode());
        target.setSort(source.getSort());
        target.setLayout(source.getLayout());
        target.setVisible(source.getVisible());
        target.setKeepAlive(source.getKeepAlive());
        target.setStatus(source.getStatus());
        return target;
    }

    private SystemRoleDO role(Long id, String code, String name) {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setSort(1);
        role.setStatus(0);
        return role;
    }
}
