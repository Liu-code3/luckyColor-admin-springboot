package com.luckycolor.admin.modules.iam.auth.service;

public interface AuthAntiAbuseService {

    void checkLoginAllowed(String tenantId, String username);

    void recordLoginFailure(String tenantId, String username);

    void clearLoginFailures(String tenantId, String username);

    void checkCaptchaAllowed(String remoteIp);

    void checkRefreshAllowed(String refreshToken);
}
