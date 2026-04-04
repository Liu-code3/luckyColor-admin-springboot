package com.luckycolor.admin.modules.tenant.tenant.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantDetailResponse;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantControllerTest {

    @Test
    void shouldReturnTenantPage() throws Exception {
        TenantService service = Mockito.mock(TenantService.class);
        when(service.pageTenants(any())).thenReturn(
            PageResult.of(
                List.of(new TenantPageResponse(
                    1L,
                    "Lucky Color",
                    10L,
                    "Liu",
                    "13800000000",
                    50,
                    LocalDateTime.of(2026, 12, 31, 23, 59),
                    0
                )),
                1L
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantController(service)).build();

        mockMvc.perform(get("/admin/tenants/page").param("name", "Lucky"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].name").value("Lucky Color"));
    }

    @Test
    void shouldReturnTenantDetail() throws Exception {
        TenantService service = Mockito.mock(TenantService.class);
        when(service.getTenant(1L)).thenReturn(
            new TenantDetailResponse(1L, "Lucky Color", 10L, "Liu", "13800000000", 50,
                LocalDateTime.of(2026, 12, 31, 23, 59), 0)
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantController(service)).build();

        mockMvc.perform(get("/admin/tenants/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Lucky Color"));
    }

    @Test
    void shouldCreateTenant() throws Exception {
        TenantService service = Mockito.mock(TenantService.class);
        when(service.createTenant(any())).thenReturn(2L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantController(service)).build();

        mockMvc.perform(post("/admin/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Lucky Color","packageId":10,"contactName":"Liu","contactMobile":"13800000000","accountCount":50,"expireTime":"2026-12-31T23:59:00","status":0}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    void shouldUpdateTenantStatus() throws Exception {
        TenantService service = Mockito.mock(TenantService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantController(service)).build();

        mockMvc.perform(put("/admin/tenants/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateTenantStatus(eq(1L), any());
    }

    @Test
    void shouldUpdateTenantExpireTime() throws Exception {
        TenantService service = Mockito.mock(TenantService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantController(service)).build();

        mockMvc.perform(put("/admin/tenants/1/expire-time")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"expireTime":"2027-01-01T00:00:00"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateTenantExpireTime(eq(1L), any());
    }
}
