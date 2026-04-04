package com.luckycolor.admin.modules.iam.audit.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.iam.audit.mapper.SecurityAuditLogMapper;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.audit.web.request.SecurityAuditLogPageQuery;
import com.luckycolor.admin.modules.iam.audit.web.response.SecurityAuditLogPageResponse;
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
@RequestMapping("/admin/security-audit-logs")
@ConditionalOnPersistenceEnabled
@Tag(name = "Security Audit Logs", description = "Security audit log query APIs")
public class SecurityAuditLogController {

    private final SecurityAuditLogService securityAuditLogService;

    public SecurityAuditLogController(SecurityAuditLogService securityAuditLogService) {
        this.securityAuditLogService = securityAuditLogService;
    }

    @GetMapping("/page")
    @RequirePermission("security:audit:query")
    @Operation(summary = "Page security audit logs")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Security audit logs loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<SecurityAuditLogPageResponse>> page(@ParameterObject SecurityAuditLogPageQuery query) {
        return ApiResponse.success(securityAuditLogService.pageSecurityAuditLogs(query));
    }
}
