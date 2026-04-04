package com.luckycolor.admin.infrastructure.security.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class JsonAuthenticationEntryPointTest {

    @Test
    void shouldWriteUnauthorizedResponseAndRecordAudit() throws Exception {
        SecurityAuditLogService securityAuditLogService = Mockito.mock(SecurityAuditLogService.class);
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint(
            new ObjectMapper().findAndRegisterModules(),
            securityAuditLogService
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/tenants/page");
        request.setAttribute(SecurityRequestAttributes.AUTH_FAILURE_REASON, "TOKEN_REVOKED");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":1011008");
        assertThat(response.getContentAsString()).contains("access token invalid, please sign in again");
        verify(securityAuditLogService).recordUnauthorized(request, "TOKEN_REVOKED");
    }
}
