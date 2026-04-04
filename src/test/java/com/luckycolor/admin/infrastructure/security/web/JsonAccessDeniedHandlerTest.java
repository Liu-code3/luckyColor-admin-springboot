package com.luckycolor.admin.infrastructure.security.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class JsonAccessDeniedHandlerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldWriteForbiddenResponseAndRecordAudit() throws Exception {
        SecurityAuditLogService securityAuditLogService = Mockito.mock(SecurityAuditLogService.class);
        JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler(
            new ObjectMapper().findAndRegisterModules(),
            securityAuditLogService
        );
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(1L, "admin", 1001L, List.of("ROLE_SUPER_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, "jwt-token", List.of())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/security-audit-logs/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"code\":1012001");
        assertThat(response.getContentAsString()).contains("permission denied");
        verify(securityAuditLogService).recordAccessDenied(
            request,
            SecurityContextHolder.getContext().getAuthentication(),
            "Forbidden"
        );
    }
}
