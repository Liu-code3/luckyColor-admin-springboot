package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.service.LoginAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginAuditLoggerService implements LoginAuditService {

    private static final Logger log = LoggerFactory.getLogger(LoginAuditLoggerService.class);

    @Override
    public void recordSuccess(String username, Long tenantId, String remoteIp) {
        log.info("login success username={} tenantId={} remoteIp={}", username, tenantId, remoteIp);
    }

    @Override
    public void recordFailure(String username, Long tenantId, String remoteIp, String reason) {
        log.warn("login failure username={} tenantId={} remoteIp={} reason={}", username, tenantId, remoteIp, reason);
    }
}
