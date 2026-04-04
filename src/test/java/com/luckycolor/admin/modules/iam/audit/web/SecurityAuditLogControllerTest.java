package com.luckycolor.admin.modules.iam.audit.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.audit.web.response.SecurityAuditLogPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SecurityAuditLogControllerTest {

    @Test
    void shouldReturnSecurityAuditLogPage() throws Exception {
        SecurityAuditLogService service = Mockito.mock(SecurityAuditLogService.class);
        when(service.pageSecurityAuditLogs(any())).thenReturn(
            PageResult.of(List.of(new SecurityAuditLogPageResponse(
                1L,
                1001L,
                1L,
                "admin",
                "LOGIN_SUCCESS",
                1,
                "POST",
                "/api/auth/login",
                "127.0.0.1",
                null,
                LocalDateTime.of(2026, 4, 1, 10, 0)
            )), 1L)
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SecurityAuditLogController(service)).build();

        mockMvc.perform(get("/admin/security-audit-logs/page").param("tenantId", "1001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].eventType").value("LOGIN_SUCCESS"))
            .andExpect(jsonPath("$.data.list[0].username").value("admin"));
    }
}
