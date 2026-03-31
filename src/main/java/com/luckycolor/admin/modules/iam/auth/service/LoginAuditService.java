package com.luckycolor.admin.modules.iam.auth.service;

public interface LoginAuditService {

    void recordSuccess(Long userId, String username, Long tenantId, String remoteIp);

    void recordFailure(Long userId, String username, Long tenantId, String remoteIp, String reason);
}
