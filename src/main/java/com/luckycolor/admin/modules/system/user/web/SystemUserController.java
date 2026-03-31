package com.luckycolor.admin.modules.system.user.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserStatusRequest;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@ConditionalOnBean(SystemUserMapper.class)
@Validated
public class SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @GetMapping("/page")
    @RequirePermission("system:user:query")
    public ApiResponse<PageResult<SystemUserPageResponse>> page(SystemUserPageQuery query) {
        return ApiResponse.success(systemUserService.pageUsers(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    public ApiResponse<SystemUserDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemUserService.getUser(id));
    }

    @GetMapping("/role-options")
    @RequirePermission("system:user:query")
    public ApiResponse<List<String>> roleOptions() {
        return ApiResponse.success(systemUserService.listRoleOptions());
    }

    @GetMapping("/export-preview")
    @RequirePermission("system:user:query")
    public ApiResponse<List<SystemUserExportPreviewResponse>> exportPreview(SystemUserPageQuery query) {
        return ApiResponse.success(systemUserService.listUsersForExportPreview(query));
    }

    @PostMapping
    @RequirePermission("system:user:create")
    public ApiResponse<Long> create(@Valid @RequestBody SystemUserSaveRequest request) {
        return ApiResponse.success(systemUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemUserSaveRequest request) {
        systemUserService.updateUser(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:update")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody SystemUserStatusRequest request) {
        systemUserService.updateUserStatus(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        systemUserService.deleteUser(id);
        return ApiResponse.success(true);
    }
}
