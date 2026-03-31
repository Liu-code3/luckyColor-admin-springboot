package com.luckycolor.admin.modules.iam.auth.service;

public interface LoginAuditService {

    void recordSuccess(String username, Long tenantId, String remoteIp);

    void recordFailure(String username, Long tenantId, String remoteIp, String reason);
}
