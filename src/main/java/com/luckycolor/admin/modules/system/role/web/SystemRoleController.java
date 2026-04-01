package com.luckycolor.admin.modules.system.role.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.role.service.SystemRoleService;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleAuthorityRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRolePageQuery;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleSaveRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleStatusRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleDetailResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRolePageResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/roles")
@ConditionalOnBean(SystemRoleMapper.class)
@Validated
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    public SystemRoleController(SystemRoleService systemRoleService) {
        this.systemRoleService = systemRoleService;
    }

    @GetMapping("/page")
    @RequirePermission("system:role:query")
    public ApiResponse<PageResult<SystemRolePageResponse>> page(SystemRolePageQuery query) {
        return ApiResponse.success(systemRoleService.pageRoles(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    public ApiResponse<SystemRoleDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemRoleService.getRole(id));
    }

    @PostMapping
    @RequirePermission("system:role:create")
    public ApiResponse<Long> create(@Valid @RequestBody SystemRoleSaveRequest request) {
        return ApiResponse.success(systemRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemRoleSaveRequest request) {
        systemRoleService.updateRole(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:role:update")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody SystemRoleStatusRequest request) {
        systemRoleService.updateRoleStatus(id, request);
        return ApiResponse.success(true);
    }

    @GetMapping("/{id}/authority")
    @RequirePermission("system:role:query")
    public ApiResponse<SystemRoleAuthorityResponse> authority(@PathVariable Long id) {
        return ApiResponse.success(systemRoleService.getRoleAuthority(id));
    }

    @PutMapping("/{id}/authority")
    @RequirePermission("system:role:authorize")
    public ApiResponse<Boolean> updateAuthority(
        @PathVariable Long id,
        @Valid @RequestBody SystemRoleAuthorityRequest request
    ) {
        systemRoleService.updateRoleAuthority(id, request);
        return ApiResponse.success(true);
    }
}
