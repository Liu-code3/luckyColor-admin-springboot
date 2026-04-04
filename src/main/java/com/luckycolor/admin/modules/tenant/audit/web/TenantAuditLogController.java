package com.luckycolor.admin.modules.tenant.audit.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.audit.web.request.TenantAuditLogPageQuery;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenant-audit-logs")
@ConditionalOnPersistenceEnabled
@Tag(name = "Tenant Audit Logs", description = "Tenant audit log query APIs")
public class TenantAuditLogController {

    private final TenantAuditLogService tenantAuditLogService;

    public TenantAuditLogController(TenantAuditLogService tenantAuditLogService) {
        this.tenantAuditLogService = tenantAuditLogService;
    }

    @GetMapping("/page")
    @Operation(summary = "Page tenant audit logs")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant audit logs loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        )
    })
    public ApiResponse<PageResult<TenantAuditLogPageResponse>> page(@ParameterObject TenantAuditLogPageQuery query) {
        return ApiResponse.success(tenantAuditLogService.pageTenantAuditLogs(query));
    }
}
