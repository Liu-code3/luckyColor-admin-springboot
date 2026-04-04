package com.luckycolor.admin.modules.tenant.audit.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantAuditLogControllerTest {

    @Test
    void shouldReturnAuditLogPage() throws Exception {
        TenantAuditLogService service = Mockito.mock(TenantAuditLogService.class);
        when(service.pageTenantAuditLogs(any())).thenReturn(
            PageResult.of(List.of(new TenantAuditLogPageResponse(
                1L, 1001L, "TENANT", 1001L, "UPDATE", "Lucky Color", LocalDateTime.of(2026, 3, 31, 21, 0)
            )), 1L)
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantAuditLogController(service)).build();

        mockMvc.perform(get("/admin/tenant-audit-logs/page").param("tenantId", "1001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].action").value("UPDATE"));
    }
}
