package com.luckycolor.admin.modules.iam.audit.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.iam.audit.mapper.SecurityAuditLogMapper;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.audit.web.request.SecurityAuditLogPageQuery;
import com.luckycolor.admin.modules.iam.audit.web.response.SecurityAuditLogPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/security-audit-logs")
@ConditionalOnBean(SecurityAuditLogMapper.class)
public class SecurityAuditLogController {

    private final SecurityAuditLogService securityAuditLogService;

    public SecurityAuditLogController(SecurityAuditLogService securityAuditLogService) {
        this.securityAuditLogService = securityAuditLogService;
    }

    @GetMapping("/page")
    @RequirePermission("security:audit:query")
    public ApiResponse<PageResult<SecurityAuditLogPageResponse>> page(SecurityAuditLogPageQuery query) {
        return ApiResponse.success(securityAuditLogService.pageSecurityAuditLogs(query));
    }
}
