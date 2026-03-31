package com.luckycolor.admin.modules.iam.audit.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.iam.audit.web.request.SecurityAuditLogPageQuery;
import com.luckycolor.admin.modules.iam.audit.web.response.SecurityAuditLogPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface SecurityAuditLogService {

    PageResult<SecurityAuditLogPageResponse> pageSecurityAuditLogs(SecurityAuditLogPageQuery query);

    void recordLoginSuccess(Long userId, String username, Long tenantId, String remoteIp);

    void recordLoginFailure(Long userId, String username, Long tenantId, String remoteIp, String reason);

    void recordLogout(Long userId, String username, Long tenantId, String remoteIp);

    void recordUnauthorized(HttpServletRequest request, String reason);

    void recordAccessDenied(HttpServletRequest request, Authentication authentication, String reason);
}
