package com.luckycolor.admin.modules.tenant.bootstrap.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.bootstrap.mapper.TenantBootstrapRecordMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapExecuteRequest;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapRecordPageQuery;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@ConditionalOnBean({TenantMapper.class, TenantBootstrapRecordMapper.class})
public class TenantBootstrapController {

    private final TenantBootstrapService tenantBootstrapService;

    public TenantBootstrapController(TenantBootstrapService tenantBootstrapService) {
        this.tenantBootstrapService = tenantBootstrapService;
    }

    @GetMapping("/admin/tenant-bootstrap/templates")
    public ApiResponse<List<TenantBootstrapTemplateResponse>> templates() {
        return ApiResponse.success(tenantBootstrapService.listTemplates());
    }

    @GetMapping("/admin/tenant-bootstrap/records/page")
    public ApiResponse<PageResult<TenantBootstrapRecordResponse>> page(TenantBootstrapRecordPageQuery query) {
        return ApiResponse.success(tenantBootstrapService.pageRecords(query));
    }

    @PostMapping("/admin/tenants/{tenantId}/bootstrap")
    public ApiResponse<TenantBootstrapRecordResponse> bootstrap(
        @PathVariable Long tenantId,
        @Valid @RequestBody TenantBootstrapExecuteRequest request
    ) {
        return ApiResponse.success(tenantBootstrapService.bootstrapTenant(tenantId, request));
    }
}
