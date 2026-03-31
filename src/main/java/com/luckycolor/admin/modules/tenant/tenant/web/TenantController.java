package com.luckycolor.admin.modules.tenant.tenant.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantExpireTimeRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantSaveRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantStatusRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantDetailResponse;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
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
@RequestMapping("/admin/tenants")
@ConditionalOnBean(TenantMapper.class)
@Validated
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TenantPageResponse>> page(TenantPageQuery query) {
        return ApiResponse.success(tenantService.pageTenants(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<TenantDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(tenantService.getTenant(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TenantSaveRequest request) {
        return ApiResponse.success(tenantService.createTenant(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody TenantSaveRequest request) {
        tenantService.updateTenant(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody TenantStatusRequest request) {
        tenantService.updateTenantStatus(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/expire-time")
    public ApiResponse<Boolean> updateExpireTime(
        @PathVariable Long id,
        @Valid @RequestBody TenantExpireTimeRequest request
    ) {
        tenantService.updateTenantExpireTime(id, request);
        return ApiResponse.success(true);
    }
}
