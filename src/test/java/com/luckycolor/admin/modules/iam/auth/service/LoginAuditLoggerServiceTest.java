package com.luckycolor.admin.modules.iam.auth.service;

import static org.mockito.Mockito.verify;

import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.auth.service.impl.LoginAuditLoggerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LoginAuditLoggerServiceTest {

    @Test
    void shouldDelegateLoginSuccessAndFailureToSecurityAuditService() {
        SecurityAuditLogService securityAuditLogService = Mockito.mock(SecurityAuditLogService.class);
        LoginAuditService service = new LoginAuditLoggerService(securityAuditLogService);

        service.recordSuccess(1L, "admin", 1001L, "127.0.0.1");
        service.recordFailure(1L, "admin", 1001L, "127.0.0.1", "PASSWORD_MISMATCH");

        verify(securityAuditLogService).recordLoginSuccess(1L, "admin", 1001L, "127.0.0.1");
        verify(securityAuditLogService).recordLoginFailure(1L, "admin", 1001L, "127.0.0.1", "PASSWORD_MISMATCH");
    }
}
