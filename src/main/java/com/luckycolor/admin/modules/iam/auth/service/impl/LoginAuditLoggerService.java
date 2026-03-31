package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.auth.service.LoginAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class LoginAuditLoggerService implements LoginAuditService {

    private static final Logger log = LoggerFactory.getLogger(LoginAuditLoggerService.class);

    private final SecurityAuditLogService securityAuditLogService;

    public LoginAuditLoggerService(@Nullable SecurityAuditLogService securityAuditLogService) {
        this.securityAuditLogService = securityAuditLogService;
    }

    @Override
    public void recordSuccess(Long userId, String username, Long tenantId, String remoteIp) {
        log.info("login success username={} tenantId={} remoteIp={}", username, tenantId, remoteIp);
        if (securityAuditLogService != null) {
            securityAuditLogService.recordLoginSuccess(userId, username, tenantId, remoteIp);
        }
    }

    @Override
    public void recordFailure(Long userId, String username, Long tenantId, String remoteIp, String reason) {
        log.warn("login failure username={} tenantId={} remoteIp={} reason={}", username, tenantId, remoteIp, reason);
        if (securityAuditLogService != null) {
            securityAuditLogService.recordLoginFailure(userId, username, tenantId, remoteIp, reason);
        }
    }
}
