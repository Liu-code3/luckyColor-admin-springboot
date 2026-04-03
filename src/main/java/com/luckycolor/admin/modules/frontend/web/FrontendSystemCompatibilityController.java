package com.luckycolor.admin.modules.frontend.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import com.luckycolor.admin.common.page.PageQuery;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.tenant.core.TenantIgnoreContextHolder;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import com.luckycolor.admin.modules.system.department.dataobject.SystemDepartmentDO;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentSaveRequest;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.role.service.SystemRoleService;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleAuthorityRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleSaveRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserAssignRolesRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserResetPasswordRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@ConditionalOnPersistenceEnabled
public class FrontendSystemCompatibilityController {
    private static final Pattern NON_CODE_CHARS = Pattern.compile("[^a-z0-9]+");

    private final SystemUserService systemUserService;
    private final SystemUserMapper systemUserMapper;
    private final SystemRoleService systemRoleService;
    private final SystemRoleMapper systemRoleMapper;
    private final SystemDepartmentService systemDepartmentService;
    private final SystemDepartmentMapper systemDepartmentMapper;
    private final MenuService menuService;
    private final MenuMapper menuMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;
    private final ObjectMapper objectMapper;

    public FrontendSystemCompatibilityController(
        SystemUserService systemUserService,
        SystemUserMapper systemUserMapper,
        SystemRoleService systemRoleService,
        SystemRoleMapper systemRoleMapper,
        SystemDepartmentService systemDepartmentService,
        SystemDepartmentMapper systemDepartmentMapper,
        MenuService menuService,
        MenuMapper menuMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        ObjectMapper objectMapper
    ) {
        this.systemUserService = systemUserService;
        this.systemUserMapper = systemUserMapper;
        this.systemRoleService = systemRoleService;
        this.systemRoleMapper = systemRoleMapper;
        this.systemDepartmentService = systemDepartmentService;
        this.systemDepartmentMapper = systemDepartmentMapper;
        this.menuService = menuService;
        this.menuMapper = menuMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/users")
    @RequirePermission("system:user:query")
    public ApiResponse<FrontendPageResult<FrontendUserRecord>> pageUsers(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        PageResult<SystemUserDO> pageResult = systemUserMapper.selectPageResult(pageQuery, buildUserQuery(keyword));
        Map<Long, SystemDepartmentDO> departments = loadDepartmentsByIds(
            pageResult.getList().stream().map(SystemUserDO::getDepartmentId).filter(Objects::nonNull).toList()
        );
        List<FrontendUserRecord> records = pageResult.getList().stream()
            .map(user -> toFrontendUser(user, departments.get(user.getDepartmentId())))
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, pageResult.getTotal(), records));
    }

    @GetMapping("/users/{id}")
    @RequirePermission("system:user:query")
    public ApiResponse<FrontendUserRecord> getUser(@PathVariable Long id) {
        SystemUserDO user = getRequiredUser(id);
        return ApiResponse.success(toFrontendUser(user, loadDepartment(user.getDepartmentId())));
    }

    @PostMapping("/users")
    @RequirePermission("system:user:create")
    public ApiResponse<FrontendUserRecord> createUser(@Valid @RequestBody FrontendUserUpsertRequest request) {
        Long id = systemUserService.createUser(toNativeUserSaveRequest(request));
        return ApiResponse.success(toFrontendUser(getRequiredUser(id), loadDepartment(request.getDepartmentId())));
    }

    @PatchMapping("/users/{id}")
    @RequirePermission("system:user:update")
    public ApiResponse<FrontendUserRecord> updateUser(
        @PathVariable Long id,
        @RequestBody FrontendUserPatchRequest request
    ) {
        SystemUserDO current = getRequiredUser(id);
        if (hasUserProfileChanges(request)) {
            systemUserService.updateUser(id, mergeUserSaveRequest(current, request));
        }
        if (StringUtils.hasText(request.getPassword())) {
            SystemUserResetPasswordRequest passwordRequest = new SystemUserResetPasswordRequest();
            passwordRequest.setNewPassword(request.getPassword().trim());
            systemUserService.resetPassword(id, passwordRequest);
        }
        SystemUserDO updated = getRequiredUser(id);
        return ApiResponse.success(toFrontendUser(updated, loadDepartment(updated.getDepartmentId())));
    }

    @DeleteMapping("/users/{id}")
    @RequirePermission("system:user:delete")
    public ApiResponse<Boolean> deleteUser(@PathVariable Long id) {
        systemUserService.deleteUser(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/users/{id}/roles")
    @RequirePermission("system:user:query")
    public ApiResponse<FrontendUserRoleAssignment> getUserRoles(@PathVariable Long id) {
        SystemUserDO user = getRequiredUser(id);
        List<SystemRoleDO> assignedRoles = findRolesByCodes(splitCodes(user.getRoleCodes()));
        return ApiResponse.success(toFrontendUserRoleAssignment(user, assignedRoles));
    }

    @PutMapping("/users/{id}/roles")
    @RequirePermission("system:user:assign-role")
    public ApiResponse<FrontendUserRoleAssignment> assignUserRoles(
        @PathVariable Long id,
        @Valid @RequestBody FrontendRoleIdsRequest request
    ) {
        getRequiredUser(id);
        List<Long> roleIds = parseRoleIds(request.getRoleIds());
        List<SystemRoleDO> roles = roleIds.isEmpty() ? List.of() : loadRolesByIdsStrict(roleIds);
        SystemUserAssignRolesRequest nativeRequest = new SystemUserAssignRolesRequest();
        nativeRequest.setRoleCodes(roles.stream().map(SystemRoleDO::getRoleCode).toList());
        systemUserService.assignRoles(id, nativeRequest);
        SystemUserDO updated = getRequiredUser(id);
        return ApiResponse.success(toFrontendUserRoleAssignment(updated, roles));
    }

    @GetMapping("/roles")
    @RequirePermission("system:role:query")
    public ApiResponse<FrontendPageResult<FrontendRoleRecord>> pageRoles(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        PageResult<SystemRoleDO> pageResult = systemRoleMapper.selectPageResult(pageQuery, buildRoleQuery(keyword));
        List<FrontendRoleRecord> records = pageResult.getList().stream()
            .map(this::toFrontendRole)
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, pageResult.getTotal(), records));
    }

    @GetMapping("/roles/{id}")
    @RequirePermission("system:role:query")
    public ApiResponse<FrontendRoleRecord> getRole(@PathVariable Long id) {
        return ApiResponse.success(toFrontendRole(getRequiredRole(id)));
    }

    @PostMapping("/roles")
    @RequirePermission("system:role:create")
    public ApiResponse<FrontendRoleRecord> createRole(@Valid @RequestBody FrontendRoleUpsertRequest request) {
        Long id = systemRoleService.createRole(toNativeRoleSaveRequest(request));
        return ApiResponse.success(toFrontendRole(getRequiredRole(id)));
    }

    @PatchMapping("/roles/{id}")
    @RequirePermission("system:role:update")
    public ApiResponse<FrontendRoleRecord> updateRole(
        @PathVariable Long id,
        @RequestBody FrontendRolePatchRequest request
    ) {
        SystemRoleDO current = getRequiredRole(id);
        systemRoleService.updateRole(id, mergeRoleSaveRequest(current, request));
        return ApiResponse.success(toFrontendRole(getRequiredRole(id)));
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("system:role:delete")
    public ApiResponse<Boolean> deleteRole(@PathVariable Long id) {
        SystemRoleDO role = getRequiredRole(id);
        removeRoleCodeFromUsers(role.getRoleCode());
        systemRoleMapper.deleteById(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/roles/{id}/menus")
    @RequirePermission("system:role:query")
    public ApiResponse<FrontendRoleMenuAssignment> getRoleMenus(@PathVariable Long id) {
        SystemRoleDO role = getRequiredRole(id);
        SystemRoleAuthorityResponse authority = systemRoleService.getRoleAuthority(id);
        List<FrontendMenuRecord> assignedMenus = findMenusByIds(authority.menuIds()).stream()
            .map(this::toFrontendMenu)
            .toList();
        return ApiResponse.success(new FrontendRoleMenuAssignment(
            String.valueOf(role.getId()),
            role.getRoleName(),
            role.getRoleCode(),
            authority.menuIds(),
            assignedMenus
        ));
    }

    @PutMapping("/roles/{id}/menus")
    @RequirePermission("system:role:authorize")
    public ApiResponse<FrontendRoleMenuAssignment> assignRoleMenus(
        @PathVariable Long id,
        @Valid @RequestBody FrontendMenuIdsRequest request
    ) {
        SystemRoleAuthorityResponse authority = systemRoleService.getRoleAuthority(id);
        SystemRoleAuthorityRequest nativeRequest = new SystemRoleAuthorityRequest();
        nativeRequest.setMenuIds(distinctLongIds(request.getMenuIds()));
        nativeRequest.setPermissionCodes(authority.permissionCodes());
        nativeRequest.setDataScope(authority.dataScope());
        nativeRequest.setDepartmentId(authority.departmentId());
        nativeRequest.setDepartmentIds(authority.departmentIds());
        systemRoleService.updateRoleAuthority(id, nativeRequest);
        return getRoleMenus(id);
    }

    @GetMapping("/roles/{id}/data-scope")
    @RequirePermission("system:role:query")
    public ApiResponse<FrontendRoleDataScopeAssignment> getRoleDataScope(@PathVariable Long id) {
        SystemRoleDO role = getRequiredRole(id);
        SystemRoleAuthorityResponse authority = systemRoleService.getRoleAuthority(id);
        return ApiResponse.success(new FrontendRoleDataScopeAssignment(
            String.valueOf(role.getId()),
            role.getRoleName(),
            role.getRoleCode(),
            toFrontendDataScope(authority.dataScope()),
            authority.departmentIds()
        ));
    }

    @PutMapping("/roles/{id}/data-scope")
    @RequirePermission("system:role:authorize")
    public ApiResponse<FrontendRoleDataScopeAssignment> updateRoleDataScope(
        @PathVariable Long id,
        @RequestBody FrontendRoleDataScopeRequest request
    ) {
        SystemRoleAuthorityResponse authority = systemRoleService.getRoleAuthority(id);
        SystemRoleAuthorityRequest nativeRequest = new SystemRoleAuthorityRequest();
        nativeRequest.setMenuIds(authority.menuIds());
        nativeRequest.setPermissionCodes(authority.permissionCodes());
        nativeRequest.setDataScope(toNativeDataScope(request.getDataScopeType()));
        nativeRequest.setDepartmentId(authority.departmentId());
        nativeRequest.setDepartmentIds(
            "CUSTOM".equals(nativeRequest.getDataScope()) ? distinctLongIds(request.getCustomDeptIds()) : List.of()
        );
        systemRoleService.updateRoleAuthority(id, nativeRequest);
        return getRoleDataScope(id);
    }

    @GetMapping("/departments")
    @RequirePermission("system:department:query")
    public ApiResponse<FrontendPageResult<FrontendDepartmentRecord>> pageDepartments(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        List<SystemDepartmentDO> departments = listScopedDepartments();
        List<FrontendDepartmentRecord> filtered = departments.stream()
            .map(this::toFrontendDepartment)
            .filter(record -> matchesDepartmentKeyword(record, keyword))
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, filtered));
    }

    @GetMapping("/departments/tree")
    @RequirePermission("system:department:query")
    public ApiResponse<List<FrontendDepartmentTreeRecord>> departmentTree() {
        List<SystemDepartmentDO> departments = listScopedDepartments();
        return ApiResponse.success(buildDepartmentTree(departments, 0L));
    }

    @GetMapping("/departments/{id}")
    @RequirePermission("system:department:query")
    public ApiResponse<FrontendDepartmentRecord> getDepartment(@PathVariable Long id) {
        return ApiResponse.success(toFrontendDepartment(getRequiredDepartment(id)));
    }

    @PostMapping("/departments")
    @RequirePermission("system:department:create")
    public ApiResponse<FrontendDepartmentRecord> createDepartment(
        @Valid @RequestBody FrontendDepartmentUpsertRequest request
    ) {
        Long id = systemDepartmentService.createDepartment(toNativeDepartmentSaveRequest(request));
        return ApiResponse.success(toFrontendDepartment(getRequiredDepartment(id)));
    }

    @PatchMapping("/departments/{id}")
    @RequirePermission("system:department:update")
    public ApiResponse<FrontendDepartmentRecord> updateDepartment(
        @PathVariable Long id,
        @RequestBody FrontendDepartmentPatchRequest request
    ) {
        SystemDepartmentDO current = getRequiredDepartment(id);
        systemDepartmentService.updateDepartment(id, mergeDepartmentSaveRequest(current, request));
        return ApiResponse.success(toFrontendDepartment(getRequiredDepartment(id)));
    }

    @DeleteMapping("/departments/{id}")
    @RequirePermission("system:department:delete")
    public ApiResponse<Boolean> deleteDepartment(@PathVariable Long id) {
        systemDepartmentService.deleteDepartment(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/menus")
    @RequirePermission("system:menu:query")
    public ApiResponse<FrontendPageResult<FrontendMenuRecord>> pageMenus(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "title", required = false) String title
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        List<FrontendMenuRecord> filtered = listMenus().stream()
            .map(this::toFrontendMenu)
            .filter(record -> matchesMenuKeyword(record, title))
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, filtered));
    }

    @GetMapping("/menus/tree")
    @RequirePermission("system:menu:query")
    public ApiResponse<List<FrontendMenuRecord>> menuTree(
        @RequestParam(value = "view", required = false) String view,
        @RequestParam(value = "roleId", required = false) Long roleId
    ) {
        List<MenuDO> nativeMenus = listMenus();
        List<MenuDO> scopedMenus = resolveMenuTreeScope(nativeMenus, view, roleId);
        return ApiResponse.success(buildMenuTree(scopedMenus, 0L, null));
    }

    @GetMapping("/menus/{id}")
    @RequirePermission("system:menu:query")
    public ApiResponse<FrontendMenuRecord> getMenu(@PathVariable Long id) {
        MenuDO menu = getRequiredMenu(id);
        Map<Long, MenuDO> allMenus = listMenus().stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        return ApiResponse.success(toFrontendMenu(menu, allMenus));
    }

    @PostMapping("/menus")
    @RequirePermission("system:menu:create")
    public ApiResponse<FrontendMenuRecord> createMenu(@Valid @RequestBody FrontendMenuUpsertRequest request) {
        Long id = menuService.createMenu(toNativeMenuSaveRequest(request));
        return getMenu(id);
    }

    @PatchMapping("/menus/{id}")
    @RequirePermission("system:menu:update")
    public ApiResponse<FrontendMenuRecord> updateMenu(
        @PathVariable Long id,
        @RequestBody FrontendMenuPatchRequest request
    ) {
        MenuDO current = getRequiredMenu(id);
        menuService.updateMenu(id, mergeMenuSaveRequest(current, request));
        return getMenu(id);
    }

    @PutMapping("/menus/sync")
    @RequirePermission("system:menu:update")
    @Transactional
    public ApiResponse<List<FrontendMenuRecord>> syncMenus(
        @Valid @RequestBody FrontendMenuSyncRequest request
    ) {
        List<MenuDO> currentMenus = listMenus();
        Map<Long, MenuDO> menusById = currentMenus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        validateSyncRequest(request.getMenus(), menusById);
        for (FrontendMenuSyncItemRequest item : request.getMenus()) {
            MenuDO menu = menusById.get(item.getId());
            menu.setParentId(normalizeParentId(item.getParentId()));
            menu.setSort(item.getSort());
            menuMapper.updateById(menu);
        }
        return ApiResponse.success(buildMenuTree(listMenus(), 0L, null));
    }

    @PatchMapping("/menus/{id}/status")
    @RequirePermission("system:menu:update")
    public ApiResponse<FrontendMenuRecord> updateMenuStatus(
        @PathVariable Long id,
        @Valid @RequestBody FrontendMenuStatusRequest request
    ) {
        getRequiredMenu(id);
        MenuStatusRequest nativeRequest = new MenuStatusRequest();
        nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
        menuService.updateMenuStatus(id, nativeRequest);
        return getMenu(id);
    }

    @DeleteMapping("/menus/{id}")
    @RequirePermission("system:menu:delete")
    public ApiResponse<Boolean> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success(true);
    }

    private LambdaQueryWrapper<SystemUserDO> buildUserQuery(String keyword) {
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(StringUtils.hasText(keyword), wrapper -> wrapper
            .like(SystemUserDO::getUsername, keyword.trim())
            .or()
            .like(SystemUserDO::getNickname, keyword.trim())
        );
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemUserDO::getTenantId, SystemUserDO::getDepartmentId);
        queryWrapper.orderByDesc(SystemUserDO::getUpdateTime).orderByDesc(SystemUserDO::getId);
        return queryWrapper;
    }

    private LambdaQueryWrapper<SystemRoleDO> buildRoleQuery(String keyword) {
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(StringUtils.hasText(keyword), wrapper -> wrapper
            .like(SystemRoleDO::getRoleName, keyword.trim())
            .or()
            .like(SystemRoleDO::getRoleCode, keyword.trim())
        );
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemRoleDO::getTenantId, null);
        queryWrapper.orderByAsc(SystemRoleDO::getSort).orderByDesc(SystemRoleDO::getUpdateTime);
        return queryWrapper;
    }

    private List<SystemDepartmentDO> listScopedDepartments() {
        LambdaQueryWrapper<SystemDepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemDepartmentDO::getTenantId, SystemDepartmentDO::getId);
        queryWrapper.orderByAsc(SystemDepartmentDO::getParentId)
            .orderByAsc(SystemDepartmentDO::getSort)
            .orderByAsc(SystemDepartmentDO::getId);
        return systemDepartmentMapper.selectList(queryWrapper);
    }

    private List<MenuDO> listMenus() {
        TenantIgnoreContextHolder.enter();
        try {
            LambdaQueryWrapper<MenuDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(MenuDO::getParentId).orderByAsc(MenuDO::getSort).orderByAsc(MenuDO::getId);
            return menuMapper.selectList(queryWrapper);
        } finally {
            TenantIgnoreContextHolder.exit();
        }
    }

    private PageQuery buildPageQuery(Long page, Long size) {
        PageQuery query = new PageQuery();
        query.setPageNo(page);
        query.setPageSize(size);
        return query;
    }

    private <T> FrontendPageResult<T> toFrontendPage(PageQuery pageQuery, long total, List<T> records) {
        return new FrontendPageResult<>(total, pageQuery.resolvePageNo(), pageQuery.resolvePageSize(), records);
    }

    private <T> FrontendPageResult<T> toFrontendPage(PageQuery pageQuery, List<T> records) {
        long total = records.size();
        long pageNo = pageQuery.resolvePageNo();
        long pageSize = pageQuery.resolvePageSize();
        int fromIndex = (int) Math.min(Math.max((pageNo - 1L) * pageSize, 0L), total);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        List<T> paged = records.subList(fromIndex, toIndex);
        return new FrontendPageResult<>(total, pageNo, pageSize, paged);
    }

    private FrontendUserRecord toFrontendUser(SystemUserDO user, SystemDepartmentDO department) {
        return new FrontendUserRecord(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getNickname(),
            stringify(user.getTenantId()),
            null,
            user.getStatus() == null || user.getStatus() == 0,
            user.getDepartmentId(),
            department == null ? null : department.getDepartmentName(),
            toIsoInstant(user.getCreateTime()),
            toIsoInstant(user.getUpdateTime())
        );
    }

    private FrontendRoleRecord toFrontendRole(SystemRoleDO role) {
        return new FrontendRoleRecord(
            String.valueOf(role.getId()),
            role.getRoleName(),
            role.getRoleCode(),
            stringify(role.getTenantId()),
            null,
            role.getSort() == null ? 0 : role.getSort(),
            role.getStatus() == null || role.getStatus() == 0,
            role.getRemark(),
            toIsoInstant(role.getCreateTime()),
            toIsoInstant(role.getUpdateTime())
        );
    }

    private FrontendDepartmentRecord toFrontendDepartment(SystemDepartmentDO department) {
        return new FrontendDepartmentRecord(
            normalizeParentId(department.getParentId()),
            department.getId(),
            deriveDepartmentCode(department),
            department.getDepartmentName(),
            stringify(department.getTenantId()),
            null,
            department.getLeader(),
            department.getPhone(),
            department.getEmail(),
            department.getSort() == null ? 0 : department.getSort(),
            department.getStatus() == null || department.getStatus() == 0,
            department.getRemark(),
            toIsoInstant(department.getCreateTime()),
            toIsoInstant(department.getUpdateTime())
        );
    }

    private FrontendDepartmentTreeRecord toFrontendDepartmentTree(
        SystemDepartmentDO department,
        List<FrontendDepartmentTreeRecord> children
    ) {
        FrontendDepartmentRecord record = toFrontendDepartment(department);
        return new FrontendDepartmentTreeRecord(
            record.pid(),
            record.id(),
            record.code(),
            record.name(),
            record.tenantId(),
            record.tenantName(),
            record.leader(),
            record.phone(),
            record.email(),
            record.sort(),
            record.status(),
            record.remark(),
            record.createdAt(),
            record.updatedAt(),
            children
        );
    }

    private FrontendMenuRecord toFrontendMenu(MenuDO menu) {
        Map<Long, MenuDO> menus = listMenus().stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        return toFrontendMenu(menu, menus);
    }

    private FrontendMenuRecord toFrontendMenu(MenuDO menu, Map<Long, MenuDO> menusById) {
        boolean keepAlive = menu.getKeepAlive() != null && menu.getKeepAlive() == 1;
        boolean isVisible = menu.getVisible() == null || menu.getVisible() == 1;
        String menuKey = resolveMenuKey(menu);
        Map<String, Object> meta = resolveMenuMeta(menu, keepAlive, isVisible);
        return new FrontendMenuRecord(
            normalizeParentId(menu.getParentId()),
            menu.getId(),
            menu.getMenuName(),
            defaultString(menu.getRouteName(), "menu" + menu.getId()),
            toFrontendMenuType(menu.getMenuType()),
            resolveMenuPath(menu, menusById),
            menuKey,
            resolvePermissionCode(menu, menuKey),
            defaultString(menu.getIcon(), ""),
            defaultString(menu.getLayout(), "default"),
            isVisible,
            menu.getStatus() == null || menu.getStatus() == 0,
            defaultString(menu.getComponent(), ""),
            emptyToNull(menu.getRedirect()),
            meta,
            menu.getSort() == null ? 0 : menu.getSort(),
            toIsoInstant(menu.getCreateTime()),
            toIsoInstant(menu.getUpdateTime()),
            List.of()
        );
    }

    private List<FrontendDepartmentTreeRecord> buildDepartmentTree(List<SystemDepartmentDO> departments, Long parentId) {
        return departments.stream()
            .filter(item -> normalizeParentId(item.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(SystemDepartmentDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SystemDepartmentDO::getId))
            .map(item -> toFrontendDepartmentTree(item, buildDepartmentTree(departments, item.getId())))
            .toList();
    }

    private List<FrontendMenuRecord> buildMenuTree(List<MenuDO> menus, Long parentId, Map<Long, MenuDO> cache) {
        Map<Long, MenuDO> menusById = cache == null
            ? menus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item))
            : cache;
        return menus.stream()
            .filter(item -> normalizeParentId(item.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(MenuDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuDO::getId))
            .map(item -> {
                FrontendMenuRecord base = toFrontendMenu(item, menusById);
                List<FrontendMenuRecord> children = buildMenuTree(menus, item.getId(), menusById);
                return new FrontendMenuRecord(
                    base.pid(),
                    base.id(),
                    base.title(),
                    base.name(),
                    base.type(),
                    base.path(),
                    base.key(),
                    base.permissionCode(),
                    base.icon(),
                    base.layout(),
                    base.isVisible(),
                    base.status(),
                    base.component(),
                    base.redirect(),
                    base.meta(),
                    base.sort(),
                    base.createdAt(),
                    base.updatedAt(),
                    children
                );
            })
            .toList();
    }

    private List<MenuDO> resolveMenuTreeScope(List<MenuDO> menus, String view, Long roleId) {
        if (roleId != null) {
            Long tenantId = requireCurrentTenantId();
            return resolveRoleScopedMenus(menus, roleId, tenantId);
        }
        if ("tenant".equalsIgnoreCase(defaultString(view, ""))) {
            Long tenantId = requireCurrentTenantId();
            return resolveTenantScopedMenus(menus, tenantId);
        }
        return menus;
    }

    private List<MenuDO> resolveRoleScopedMenus(List<MenuDO> menus, Long roleId, Long tenantId) {
        SystemRoleDO role = systemRoleMapper.selectById(roleId);
        if (role == null || !Objects.equals(role.getTenantId(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        return expandMenusWithAncestors(menus, splitLongCodes(role.getMenuIds()));
    }

    private List<MenuDO> resolveTenantScopedMenus(List<MenuDO> menus, Long tenantId) {
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleDO::getTenantId, tenantId);
        List<SystemRoleDO> roles = systemRoleMapper.selectList(queryWrapper);
        Set<Long> menuIds = new LinkedHashSet<>();
        for (SystemRoleDO role : roles) {
            menuIds.addAll(splitLongCodes(role.getMenuIds()));
        }
        return expandMenusWithAncestors(menus, menuIds);
    }

    private List<MenuDO> expandMenusWithAncestors(List<MenuDO> menus, Collection<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return List.of();
        }
        Map<Long, MenuDO> menusById = menus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        Set<Long> expandedIds = new LinkedHashSet<>();
        for (Long selectedId : selectedIds) {
            MenuDO current = menusById.get(selectedId);
            while (current != null && expandedIds.add(current.getId())) {
                Long parentId = normalizeParentId(current.getParentId());
                current = parentId.equals(0L) ? null : menusById.get(parentId);
            }
        }
        return menus.stream()
            .filter(menu -> expandedIds.contains(menu.getId()))
            .toList();
    }

    private Long requireCurrentTenantId() {
        return TenantContextHolder.getOptionalTenantId()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant context is required"));
    }

    private List<MenuDO> findMenusByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> idSet = new LinkedHashSet<>(ids);
        Map<Long, MenuDO> menusById = listMenus().stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        List<MenuDO> result = new ArrayList<>();
        for (Long id : idSet) {
            MenuDO menu = menusById.get(id);
            if (menu != null) {
                result.add(menu);
            }
        }
        return result;
    }

    private FrontendUserRoleAssignment toFrontendUserRoleAssignment(SystemUserDO user, List<SystemRoleDO> roles) {
        return new FrontendUserRoleAssignment(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getNickname(),
            roles.stream().map(role -> String.valueOf(role.getId())).toList(),
            roles.stream()
                .map(role -> new FrontendAssignedRole(
                    String.valueOf(role.getId()),
                    role.getRoleName(),
                    role.getRoleCode(),
                    role.getSort() == null ? 0 : role.getSort(),
                    role.getStatus() == null || role.getStatus() == 0
                ))
                .toList()
        );
    }

    private SystemUserSaveRequest toNativeUserSaveRequest(FrontendUserUpsertRequest request) {
        SystemUserSaveRequest nativeRequest = new SystemUserSaveRequest();
        nativeRequest.setUsername(request.getUsername().trim());
        nativeRequest.setPassword(request.getPassword().trim());
        nativeRequest.setNickname(resolveNickname(request.getNickname(), request.getUsername()));
        nativeRequest.setEmail(null);
        nativeRequest.setMobile(null);
        nativeRequest.setRoleCodes(List.of());
        nativeRequest.setPermissionCodes(List.of());
        nativeRequest.setDataScope("TENANT");
        nativeRequest.setDepartmentId(request.getDepartmentId());
        nativeRequest.setDepartmentIds(List.of());
        nativeRequest.setScopeTenantIds(List.of());
        nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
        nativeRequest.setRemark(null);
        return nativeRequest;
    }

    private SystemUserSaveRequest mergeUserSaveRequest(SystemUserDO current, FrontendUserPatchRequest request) {
        SystemUserSaveRequest nativeRequest = new SystemUserSaveRequest();
        nativeRequest.setUsername(resolveString(request.getUsername(), current.getUsername()));
        nativeRequest.setPassword(StringUtils.hasText(request.getPassword()) ? request.getPassword().trim() : null);
        nativeRequest.setNickname(resolveNickname(request.getNickname(), current.getNickname()));
        nativeRequest.setEmail(current.getEmail());
        nativeRequest.setMobile(current.getMobile());
        nativeRequest.setRoleCodes(splitCodes(current.getRoleCodes()));
        nativeRequest.setPermissionCodes(splitCodes(current.getPermissionCodes()));
        nativeRequest.setDataScope(defaultString(current.getDataScope(), "TENANT"));
        nativeRequest.setDepartmentId(request.getDepartmentId() != null ? request.getDepartmentId() : current.getDepartmentId());
        nativeRequest.setDepartmentIds(splitLongCodes(current.getDepartmentIds()));
        nativeRequest.setScopeTenantIds(splitLongCodes(current.getScopeTenantIds()));
        nativeRequest.setStatus(request.getStatus() != null ? toNativeStatus(request.getStatus(), true) : defaultInteger(current.getStatus(), 0));
        nativeRequest.setRemark(current.getRemark());
        return nativeRequest;
    }

    private boolean hasUserProfileChanges(FrontendUserPatchRequest request) {
        return request.getUsername() != null
            || request.getNickname() != null
            || request.getDepartmentId() != null
            || request.getStatus() != null;
    }

    private SystemRoleSaveRequest toNativeRoleSaveRequest(FrontendRoleUpsertRequest request) {
        SystemRoleSaveRequest nativeRequest = new SystemRoleSaveRequest();
        nativeRequest.setRoleName(request.getName().trim());
        nativeRequest.setRoleCode(request.getCode().trim());
        nativeRequest.setSort(defaultInteger(request.getSort(), 0));
        nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
        nativeRequest.setRemark(emptyToNull(request.getRemark()));
        return nativeRequest;
    }

    private SystemRoleSaveRequest mergeRoleSaveRequest(SystemRoleDO current, FrontendRolePatchRequest request) {
        SystemRoleSaveRequest nativeRequest = new SystemRoleSaveRequest();
        nativeRequest.setRoleName(resolveString(request.getName(), current.getRoleName()));
        nativeRequest.setRoleCode(resolveString(request.getCode(), current.getRoleCode()));
        nativeRequest.setSort(request.getSort() != null ? request.getSort() : defaultInteger(current.getSort(), 0));
        nativeRequest.setStatus(request.getStatus() != null ? toNativeStatus(request.getStatus(), true) : defaultInteger(current.getStatus(), 0));
        nativeRequest.setRemark(request.getRemark() != null ? emptyToNull(request.getRemark()) : current.getRemark());
        return nativeRequest;
    }

    private SystemDepartmentSaveRequest toNativeDepartmentSaveRequest(FrontendDepartmentUpsertRequest request) {
        SystemDepartmentSaveRequest nativeRequest = new SystemDepartmentSaveRequest();
        nativeRequest.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        nativeRequest.setDepartmentName(request.getName().trim());
        nativeRequest.setLeader(emptyToNull(request.getLeader()));
        nativeRequest.setPhone(emptyToNull(request.getPhone()));
        nativeRequest.setEmail(emptyToNull(request.getEmail()));
        nativeRequest.setSort(defaultInteger(request.getSort(), 0));
        nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
        nativeRequest.setRemark(emptyToNull(request.getRemark()));
        return nativeRequest;
    }

    private SystemDepartmentSaveRequest mergeDepartmentSaveRequest(SystemDepartmentDO current, FrontendDepartmentPatchRequest request) {
        SystemDepartmentSaveRequest nativeRequest = new SystemDepartmentSaveRequest();
        nativeRequest.setParentId(request.getParentId() != null ? request.getParentId() : normalizeParentId(current.getParentId()));
        nativeRequest.setDepartmentName(resolveString(request.getName(), current.getDepartmentName()));
        nativeRequest.setLeader(request.getLeader() != null ? emptyToNull(request.getLeader()) : current.getLeader());
        nativeRequest.setPhone(request.getPhone() != null ? emptyToNull(request.getPhone()) : current.getPhone());
        nativeRequest.setEmail(request.getEmail() != null ? emptyToNull(request.getEmail()) : current.getEmail());
        nativeRequest.setSort(request.getSort() != null ? request.getSort() : defaultInteger(current.getSort(), 0));
        nativeRequest.setStatus(request.getStatus() != null ? toNativeStatus(request.getStatus(), true) : defaultInteger(current.getStatus(), 0));
        nativeRequest.setRemark(request.getRemark() != null ? emptyToNull(request.getRemark()) : current.getRemark());
        return nativeRequest;
    }

    private MenuSaveRequest toNativeMenuSaveRequest(FrontendMenuUpsertRequest request) {
        MenuSaveRequest nativeRequest = new MenuSaveRequest();
        nativeRequest.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        nativeRequest.setMenuName(request.getTitle().trim());
        nativeRequest.setMenuType(toNativeMenuType(request.getType()));
        nativeRequest.setRouteName(request.getName().trim());
        nativeRequest.setRoutePath(request.getPath().trim());
        nativeRequest.setMenuKey(emptyToNull(request.getMenuKey()));
        nativeRequest.setComponent(request.getComponent().trim());
        nativeRequest.setRedirect(emptyToNull(request.getRedirect()));
        nativeRequest.setMeta(normalizeMenuMeta(
            request.getMeta(),
            request.getTitle().trim(),
            request.getIsVisible() == null || request.getIsVisible(),
            resolveKeepAlive(request.getMeta(), 1) == 1
        ));
        nativeRequest.setPermissionCode(resolveRequestedPermissionCode(request.getMenuKey(), request.getPermissionCode()));
        nativeRequest.setRoleCodes(List.of());
        nativeRequest.setIcon(emptyToNull(request.getIcon()));
        nativeRequest.setSort(defaultInteger(request.getSort(), 0));
        nativeRequest.setVisible(toNativeVisible(request.getIsVisible(), true));
        nativeRequest.setKeepAlive(resolveKeepAlive(request.getMeta(), 1));
        nativeRequest.setAlwaysShow(0);
        nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
        nativeRequest.setLayout(emptyToNull(request.getLayout()) == null ? "default" : emptyToNull(request.getLayout()));
        nativeRequest.setRemark(null);
        return nativeRequest;
    }

    private MenuSaveRequest mergeMenuSaveRequest(MenuDO current, FrontendMenuPatchRequest request) {
        MenuSaveRequest nativeRequest = new MenuSaveRequest();
        nativeRequest.setParentId(request.getParentId() != null ? request.getParentId() : normalizeParentId(current.getParentId()));
        nativeRequest.setMenuName(resolveString(request.getTitle(), current.getMenuName()));
        nativeRequest.setMenuType(request.getType() != null ? toNativeMenuType(request.getType()) : current.getMenuType());
        nativeRequest.setRouteName(resolveString(request.getName(), current.getRouteName()));
        nativeRequest.setRoutePath(resolveString(request.getPath(), current.getRoutePath()));
        nativeRequest.setMenuKey(request.getMenuKey() != null ? emptyToNull(request.getMenuKey()) : current.getMenuKey());
        nativeRequest.setComponent(resolveString(request.getComponent(), current.getComponent()));
        nativeRequest.setRedirect(request.getRedirect() != null ? emptyToNull(request.getRedirect()) : current.getRedirect());
        boolean isVisible = request.getIsVisible() != null ? request.getIsVisible() : current.getVisible() == null || current.getVisible() == 1;
        int keepAlive = resolveKeepAlive(request.getMeta(), defaultInteger(current.getKeepAlive(), 1));
        nativeRequest.setMeta(normalizeMenuMeta(
            request.getMeta() != null ? request.getMeta() : parseMenuMeta(current.getMeta()),
            nativeRequest.getMenuName(),
            isVisible,
            keepAlive == 1
        ));
        nativeRequest.setPermissionCode(resolvePatchedPermissionCode(current, request));
        nativeRequest.setRoleCodes(splitCodes(current.getRoleCodes()));
        nativeRequest.setIcon(request.getIcon() != null ? emptyToNull(request.getIcon()) : current.getIcon());
        nativeRequest.setSort(request.getSort() != null ? request.getSort() : defaultInteger(current.getSort(), 0));
        nativeRequest.setVisible(toNativeVisible(isVisible, true));
        nativeRequest.setKeepAlive(keepAlive);
        nativeRequest.setAlwaysShow(defaultInteger(current.getAlwaysShow(), 0));
        nativeRequest.setStatus(request.getStatus() != null ? toNativeStatus(request.getStatus(), true) : defaultInteger(current.getStatus(), 0));
        nativeRequest.setLayout(request.getLayout() != null ? emptyToNull(request.getLayout()) : current.getLayout());
        nativeRequest.setRemark(current.getRemark());
        return nativeRequest;
    }

    private String resolveRequestedPermissionCode(String menuKey, String permissionCode) {
        String normalizedMenuKey = emptyToNull(menuKey);
        String normalizedPermissionCode = emptyToNull(permissionCode);
        return normalizedPermissionCode != null ? normalizedPermissionCode : normalizedMenuKey;
    }

    private String resolvePatchedPermissionCode(MenuDO current, FrontendMenuPatchRequest request) {
        if (request.getPermissionCode() == null) {
            return current.getPermissionCode();
        }
        String normalizedPermissionCode = emptyToNull(request.getPermissionCode());
        if (normalizedPermissionCode != null) {
            return normalizedPermissionCode;
        }
        if (request.getMenuKey() != null) {
            return emptyToNull(request.getMenuKey());
        }
        return resolveMenuKey(current);
    }

    private List<Long> parseRoleIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        try {
            return roleIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(Long::valueOf)
                .distinct()
                .toList();
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role id is invalid", exception);
        }
    }

    private List<Long> distinctLongIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private SystemUserDO getRequiredUser(Long id) {
        SystemUserDO user = systemUserMapper.selectById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private SystemRoleDO getRequiredRole(Long id) {
        SystemRoleDO role = systemRoleMapper.selectById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        return role;
    }

    private SystemDepartmentDO getRequiredDepartment(Long id) {
        SystemDepartmentDO department = systemDepartmentMapper.selectById(id);
        if (department == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }
        return department;
    }

    private MenuDO getRequiredMenu(Long id) {
        TenantIgnoreContextHolder.enter();
        try {
            MenuDO menu = menuMapper.selectById(id);
            if (menu == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found");
            }
            return menu;
        } finally {
            TenantIgnoreContextHolder.exit();
        }
    }

    private SystemDepartmentDO loadDepartment(Long id) {
        return id == null ? null : systemDepartmentMapper.selectById(id);
    }

    private Map<Long, SystemDepartmentDO> loadDepartmentsByIds(Collection<Long> departmentIds) {
        List<Long> ids = departmentIds == null ? List.of() : departmentIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return systemDepartmentMapper.selectBatchIds(ids).stream()
            .collect(Collectors.toMap(SystemDepartmentDO::getId, item -> item));
    }

    private List<SystemRoleDO> loadRolesByIdsStrict(List<Long> roleIds) {
        List<SystemRoleDO> roles = systemRoleMapper.selectBatchIds(roleIds);
        Map<Long, SystemRoleDO> rolesById = roles.stream()
            .collect(Collectors.toMap(SystemRoleDO::getId, item -> item));
        List<SystemRoleDO> ordered = new ArrayList<>();
        for (Long roleId : roleIds) {
            SystemRoleDO role = rolesById.get(roleId);
            if (role == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
            }
            ordered.add(role);
        }
        return ordered;
    }

    private List<SystemRoleDO> findRolesByCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SystemRoleDO::getRoleCode, roleCodes);
        Map<String, SystemRoleDO> rolesByCode = systemRoleMapper.selectList(queryWrapper).stream()
            .collect(Collectors.toMap(SystemRoleDO::getRoleCode, item -> item));
        List<SystemRoleDO> ordered = new ArrayList<>();
        for (String roleCode : roleCodes) {
            SystemRoleDO role = rolesByCode.get(roleCode);
            if (role != null) {
                ordered.add(role);
            }
        }
        return ordered;
    }

    private void removeRoleCodeFromUsers(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return;
        }
        for (SystemUserDO user : systemUserMapper.selectList(new LambdaQueryWrapper<>())) {
            List<String> currentCodes = splitCodes(user.getRoleCodes());
            List<String> remainingCodes = currentCodes.stream()
                .filter(code -> !roleCode.equals(code))
                .toList();
            if (remainingCodes.size() == currentCodes.size()) {
                continue;
            }
            user.setRoleCodes(joinCodes(remainingCodes));
            systemUserMapper.updateById(user);
        }
    }

    private boolean matchesDepartmentKeyword(FrontendDepartmentRecord record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(record.name(), normalized) || containsIgnoreCase(record.code(), normalized);
    }

    private boolean matchesMenuKeyword(FrontendMenuRecord record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(record.title(), normalized)
            || containsIgnoreCase(record.path(), normalized)
            || containsIgnoreCase(record.name(), normalized)
            || containsIgnoreCase(record.key(), normalized)
            || containsIgnoreCase(record.permissionCode(), normalized);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private List<String> splitCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return List.of(values.split(",")).stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private List<Long> splitLongCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return List.of(values.split(",")).stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(Long::valueOf)
            .toList();
    }

    private String joinCodes(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .collect(Collectors.joining(","));
    }

    private void validateSyncRequest(List<FrontendMenuSyncItemRequest> items, Map<Long, MenuDO> menusById) {
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (FrontendMenuSyncItemRequest item : items) {
            if (!uniqueIds.add(item.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate menu id in sync payload");
            }
            if (!menusById.containsKey(item.getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found");
            }
        }
        Map<Long, Long> nextParentIds = menusById.values().stream()
            .collect(Collectors.toMap(MenuDO::getId, menu -> normalizeParentId(menu.getParentId())));
        for (FrontendMenuSyncItemRequest item : items) {
            Long normalizedParentId = normalizeParentId(item.getParentId());
            if (!normalizedParentId.equals(0L) && !menusById.containsKey(normalizedParentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent menu not found");
            }
            nextParentIds.put(item.getId(), normalizedParentId);
        }
        for (Long menuId : nextParentIds.keySet()) {
            assertNoMenuCycle(menuId, nextParentIds);
        }
    }

    private void assertNoMenuCycle(Long menuId, Map<Long, Long> nextParentIds) {
        Set<Long> visited = new HashSet<>();
        Long currentId = menuId;
        while (true) {
            Long parentId = normalizeParentId(nextParentIds.get(currentId));
            if (parentId.equals(0L)) {
                return;
            }
            if (!visited.add(parentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu hierarchy cycle detected");
            }
            if (!nextParentIds.containsKey(parentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent menu not found");
            }
            currentId = parentId;
        }
    }

    private String resolveMenuPath(MenuDO menu, Map<Long, MenuDO> menusById) {
        String currentPath = defaultString(menu.getRoutePath(), "");
        if (currentPath.startsWith("/")) {
            return currentPath;
        }
        Long parentId = normalizeParentId(menu.getParentId());
        if (parentId == 0L) {
            return currentPath.startsWith("/") ? currentPath : "/" + currentPath;
        }
        MenuDO parent = menusById.get(parentId);
        if (parent == null) {
            return currentPath.startsWith("/") ? currentPath : "/" + currentPath;
        }
        String parentPath = resolveMenuPath(parent, menusById);
        if (!StringUtils.hasText(currentPath)) {
            return parentPath;
        }
        if (parentPath.endsWith("/")) {
            return parentPath + currentPath;
        }
        return parentPath + "/" + currentPath;
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

    private String resolvePermissionCode(MenuDO menu, String fallback) {
        if (menu != null && StringUtils.hasText(menu.getPermissionCode())) {
            return menu.getPermissionCode().trim();
        }
        return fallback;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String deriveDepartmentCode(SystemDepartmentDO department) {
        String base = defaultString(department.getDepartmentName(), "department");
        String normalized = NON_CODE_CHARS.matcher(base.toLowerCase(Locale.ROOT)).replaceAll("_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(normalized)) {
            normalized = "department_" + department.getId();
        }
        return normalized;
    }

    private Integer resolveKeepAlive(Map<String, Object> meta, Integer fallback) {
        if (meta == null || meta.isEmpty()) {
            return fallback;
        }
        Object keepAlive = meta.get("keepAlive");
        if (keepAlive instanceof Boolean value) {
            return value ? 1 : 0;
        }
        return fallback;
    }

    private Map<String, Object> resolveMenuMeta(MenuDO menu, boolean keepAlive, boolean isVisible) {
        Map<String, Object> meta = normalizeMenuMeta(parseMenuMeta(menu.getMeta()), menu.getMenuName(), isVisible, keepAlive);
        return meta.isEmpty() ? null : meta;
    }

    private Map<String, Object> normalizeMenuMeta(
        Map<String, Object> source,
        String title,
        boolean isVisible,
        boolean keepAlive
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (source != null && !source.isEmpty()) {
            values.putAll(source);
        }
        values.put("title", title);
        values.put("keepAlive", keepAlive);
        values.put("hidden", !isVisible);
        return values;
    }

    private Map<String, Object> parseMenuMeta(String value) {
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

    private Map<String, Object> buildMenuMeta(String title, boolean keepAlive, boolean hidden) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("title", title);
        values.put("keepAlive", keepAlive);
        values.put("hidden", hidden);
        return values;
    }

    private int toFrontendMenuType(String menuType) {
        if (!StringUtils.hasText(menuType)) {
            return 2;
        }
        return switch (menuType.trim().toUpperCase(Locale.ROOT)) {
            case "DIRECTORY" -> 1;
            case "BUTTON" -> 3;
            default -> 2;
        };
    }

    private String toNativeMenuType(Integer type) {
        if (type == null) {
            return "MENU";
        }
        return switch (type) {
            case 1 -> "DIRECTORY";
            case 3 -> "BUTTON";
            default -> "MENU";
        };
    }

    private String toFrontendDataScope(String nativeValue) {
        if (!StringUtils.hasText(nativeValue)) {
            return "TENANT";
        }
        return switch (nativeValue.trim().toUpperCase(Locale.ROOT)) {
            case "DEPARTMENT" -> "DEPT";
            case "DEPARTMENT_AND_CHILDREN" -> "DEPT_AND_CHILD";
            default -> nativeValue.trim().toUpperCase(Locale.ROOT);
        };
    }

    private String toNativeDataScope(String frontendValue) {
        if (!StringUtils.hasText(frontendValue)) {
            return "TENANT";
        }
        return switch (frontendValue.trim().toUpperCase(Locale.ROOT)) {
            case "DEPT" -> "DEPARTMENT";
            case "DEPT_AND_CHILD" -> "DEPARTMENT_AND_CHILDREN";
            case "SELF" -> "DEPARTMENT";
            default -> frontendValue.trim().toUpperCase(Locale.ROOT);
        };
    }

    private String resolveNickname(String nickname, String fallback) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        return defaultString(fallback, "user");
    }

    private String resolveString(String preferred, String fallback) {
        return preferred != null ? preferred.trim() : fallback;
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer defaultInteger(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private Integer toNativeStatus(Boolean value, boolean defaultValue) {
        boolean resolved = value == null ? defaultValue : value;
        return resolved ? 0 : 1;
    }

    private Integer toNativeVisible(Boolean value, boolean defaultValue) {
        boolean resolved = value == null ? defaultValue : value;
        return resolved ? 1 : 0;
    }

    private String stringify(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private String toIsoInstant(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC).toInstant().toString();
    }

    public record FrontendPageResult<T>(long total, long current, long size, List<T> records) {
    }

    public record FrontendUserRecord(
        String id,
        String username,
        String nickname,
        String tenantId,
        String tenantName,
        Boolean status,
        Long departmentId,
        String departmentName,
        String createdAt,
        String updatedAt
    ) {
    }

    public record FrontendAssignedRole(String id, String name, String code, int sort, boolean status) {
    }

    public record FrontendUserRoleAssignment(
        String userId,
        String username,
        String nickname,
        List<String> roleIds,
        List<FrontendAssignedRole> roles
    ) {
    }

    public record FrontendRoleRecord(
        String id,
        String name,
        String code,
        String tenantId,
        String tenantName,
        int sort,
        boolean status,
        String remark,
        String createdAt,
        String updatedAt
    ) {
    }

    public record FrontendRoleMenuAssignment(
        String roleId,
        String name,
        String code,
        List<Long> menuIds,
        List<FrontendMenuRecord> menus
    ) {
    }

    public record FrontendRoleDataScopeAssignment(
        String roleId,
        String name,
        String code,
        String dataScopeType,
        List<Long> customDeptIds
    ) {
    }

    public record FrontendDepartmentRecord(
        Long pid,
        Long id,
        String code,
        String name,
        String tenantId,
        String tenantName,
        String leader,
        String phone,
        String email,
        int sort,
        boolean status,
        String remark,
        String createdAt,
        String updatedAt
    ) {
    }

    public record FrontendDepartmentTreeRecord(
        Long pid,
        Long id,
        String code,
        String name,
        String tenantId,
        String tenantName,
        String leader,
        String phone,
        String email,
        int sort,
        boolean status,
        String remark,
        String createdAt,
        String updatedAt,
        List<FrontendDepartmentTreeRecord> children
    ) {
    }

    public record FrontendMenuRecord(
        Long pid,
        Long id,
        String title,
        String name,
        int type,
        String path,
        String key,
        String permissionCode,
        String icon,
        String layout,
        boolean isVisible,
        boolean status,
        String component,
        String redirect,
        Map<String, Object> meta,
        int sort,
        String createdAt,
        String updatedAt,
        List<FrontendMenuRecord> children
    ) {
    }

    @Getter
    @Setter
    public static class FrontendUserUpsertRequest {

        @NotNull
        private String username;

        @NotNull
        private String password;

        private String nickname;

        private Boolean status;

        private Long departmentId;
    }

    @Getter
    @Setter
    public static class FrontendUserPatchRequest {

        private String username;

        private String password;

        private String nickname;

        private Boolean status;

        private Long departmentId;
    }

    @Getter
    @Setter
    public static class FrontendRoleIdsRequest {

        @NotEmpty
        private List<String> roleIds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class FrontendRoleUpsertRequest {

        @NotNull
        private String name;

        @NotNull
        private String code;

        private Integer sort;

        private Boolean status;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendRolePatchRequest {

        private String name;

        private String code;

        private Integer sort;

        private Boolean status;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendMenuIdsRequest {

        private List<Long> menuIds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class FrontendRoleDataScopeRequest {

        private String dataScopeType;

        private List<Long> customDeptIds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class FrontendDepartmentUpsertRequest {

        private Long parentId;

        @NotNull
        private String name;

        private String code;

        private String leader;

        private String phone;

        private String email;

        private Integer sort;

        private Boolean status;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendDepartmentPatchRequest {

        private Long parentId;

        private String name;

        private String code;

        private String leader;

        private String phone;

        private String email;

        private Integer sort;

        private Boolean status;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendMenuUpsertRequest {

        private Long parentId;

        @NotNull
        private String title;

        @NotNull
        private String name;

        private Integer type;

        @NotNull
        private String path;

        private String menuKey;

        private String permissionCode;

        private String icon;

        private String layout;

        private Boolean isVisible;

        private Boolean status;

        @NotNull
        private String component;

        private String redirect;

        private Map<String, Object> meta = new HashMap<>();

        private Integer sort;
    }

    @Getter
    @Setter
    public static class FrontendMenuPatchRequest {

        private Long parentId;

        private String title;

        private String name;

        private Integer type;

        private String path;

        private String menuKey;

        private String permissionCode;

        private String icon;

        private String layout;

        private Boolean isVisible;

        private Boolean status;

        private String component;

        private String redirect;

        private Map<String, Object> meta = new HashMap<>();

        private Integer sort;
    }

    @Getter
    @Setter
    public static class FrontendMenuStatusRequest {

        @NotNull
        private Boolean status;
    }

    @Getter
    @Setter
    public static class FrontendMenuSyncRequest {

        @NotEmpty
        private List<FrontendMenuSyncItemRequest> menus = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class FrontendMenuSyncItemRequest {

        @NotNull
        private Long id;

        private Long parentId;

        @NotNull
        private Integer sort;
    }
}
