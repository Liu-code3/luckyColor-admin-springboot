package com.luckycolor.admin.modules.tenant.audit.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.audit.web.request.TenantAuditLogPageQuery;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenant-audit-logs")
@ConditionalOnBean(TenantAuditLogMapper.class)
public class TenantAuditLogController {

    private final TenantAuditLogService tenantAuditLogService;

    public TenantAuditLogController(TenantAuditLogService tenantAuditLogService) {
        this.tenantAuditLogService = tenantAuditLogService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TenantAuditLogPageResponse>> page(TenantAuditLogPageQuery query) {
        return ApiResponse.success(tenantAuditLogService.pageTenantAuditLogs(query));
    }
}
