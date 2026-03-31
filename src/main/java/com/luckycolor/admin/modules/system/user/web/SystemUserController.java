package com.luckycolor.admin.modules.system.user.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
