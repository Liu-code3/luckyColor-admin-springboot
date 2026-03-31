package com.luckycolor.admin.modules.tenant.packageinfo.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenant-packages")
@ConditionalOnBean(TenantPackageMapper.class)
public class TenantPackageController {

    private final TenantPackageService tenantPackageService;

    public TenantPackageController(TenantPackageService tenantPackageService) {
        this.tenantPackageService = tenantPackageService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TenantPackagePageResponse>> page(TenantPackagePageQuery query) {
        return ApiResponse.success(tenantPackageService.pageTenantPackages(query));
    }
}
