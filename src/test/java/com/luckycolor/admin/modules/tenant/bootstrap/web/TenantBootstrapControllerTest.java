package com.luckycolor.admin.modules.tenant.bootstrap.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantBootstrapControllerTest {

    @Test
    void shouldReturnTemplates() throws Exception {
        TenantBootstrapService service = Mockito.mock(TenantBootstrapService.class);
        when(service.listTemplates()).thenReturn(List.of(
            new TenantBootstrapTemplateResponse(
                "default",
                "默认租户模板",
                List.of("tenant_admin"),
                List.of("dashboard"),
                "admin",
                "租户管理员",
                0,
                "内置初始化模板"
            )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantBootstrapController(service)).build();

        mockMvc.perform(get("/admin/tenant-bootstrap/templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].code").value("default"));
    }

    @Test
    void shouldReturnBootstrapRecords() throws Exception {
        TenantBootstrapService service = Mockito.mock(TenantBootstrapService.class);
        when(service.pageRecords(any())).thenReturn(PageResult.of(
            List.of(new TenantBootstrapRecordResponse(
                2L,
                1L,
                "default",
                "默认租户模板",
                List.of("tenant_admin"),
                List.of("dashboard"),
                "admin",
                "租户管理员",
                1,
                LocalDateTime.of(2026, 3, 31, 12, 0),
                "首次初始化"
            )),
            1L
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantBootstrapController(service)).build();

        mockMvc.perform(get("/admin/tenant-bootstrap/records/page").param("tenantId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].templateCode").value("default"));
    }

    @Test
    void shouldBootstrapTenant() throws Exception {
        TenantBootstrapService service = Mockito.mock(TenantBootstrapService.class);
        when(service.bootstrapTenant(eq(1L), any())).thenReturn(
            new TenantBootstrapRecordResponse(
                2L,
                1L,
                "default",
                "默认租户模板",
                List.of("tenant_admin"),
                List.of("dashboard"),
                "admin",
                "租户管理员",
                1,
                LocalDateTime.of(2026, 3, 31, 12, 0),
                "首次初始化"
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantBootstrapController(service)).build();

        mockMvc.perform(post("/admin/tenants/1/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"templateCode":"default","adminUsername":"admin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(2))
            .andExpect(jsonPath("$.data.templateCode").value("default"));
    }
}
