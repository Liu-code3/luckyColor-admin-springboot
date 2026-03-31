package com.luckycolor.admin.modules.tenant.tenant.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenants")
@ConditionalOnBean(TenantMapper.class)
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TenantPageResponse>> page(TenantPageQuery query) {
        return ApiResponse.success(tenantService.pageTenants(query));
    }
}
