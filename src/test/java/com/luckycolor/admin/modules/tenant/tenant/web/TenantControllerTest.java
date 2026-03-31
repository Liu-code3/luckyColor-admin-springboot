package com.luckycolor.admin.modules.tenant.tenant.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].name").value("Lucky Color"));
    }
}
