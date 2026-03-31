package com.luckycolor.admin.modules.tenant.packageinfo.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageSaveRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageStatusRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackageDetailResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/admin/tenant-packages")
@ConditionalOnBean(TenantPackageMapper.class)
@Validated
public class TenantPackageController {

    private final TenantPackageService tenantPackageService;

    public TenantPackageController(TenantPackageService tenantPackageService) {
        this.tenantPackageService = tenantPackageService;
    }

    @GetMapping("/page")
    @RequirePermission("tenant:package:query")
    public ApiResponse<PageResult<TenantPackagePageResponse>> page(TenantPackagePageQuery query) {
        return ApiResponse.success(tenantPackageService.pageTenantPackages(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("tenant:package:query")
    public ApiResponse<TenantPackageDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(tenantPackageService.getTenantPackage(id));
    }

    @PostMapping
    @RequirePermission("tenant:package:create")
    public ApiResponse<Long> create(@Valid @RequestBody TenantPackageSaveRequest request) {
        return ApiResponse.success(tenantPackageService.createTenantPackage(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("tenant:package:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody TenantPackageSaveRequest request) {
        tenantPackageService.updateTenantPackage(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("tenant:package:update")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody TenantPackageStatusRequest request) {
        tenantPackageService.updateTenantPackageStatus(id, request);
        return ApiResponse.success(true);
    }
}
