package com.luckycolor.admin.modules.system.config.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.config.mapper.SystemConfigMapper;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigPageQuery;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigSaveRequest;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigDetailResponse;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;
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
@RequestMapping("/admin/system-configs")
@ConditionalOnBean(SystemConfigMapper.class)
@Validated
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/page")
    @RequirePermission("system:config:query")
    public ApiResponse<PageResult<SystemConfigPageResponse>> page(SystemConfigPageQuery query) {
        return ApiResponse.success(systemConfigService.pageConfigs(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:config:query")
    public ApiResponse<SystemConfigDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemConfigService.getConfig(id));
    }

    @PostMapping
    @RequirePermission("system:config:create")
    public ApiResponse<Long> create(@Valid @RequestBody SystemConfigSaveRequest request) {
        return ApiResponse.success(systemConfigService.createConfig(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:config:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemConfigSaveRequest request) {
        systemConfigService.updateConfig(id, request);
        return ApiResponse.success(true);
    }
}
