package com.luckycolor.admin.modules.tenant.packageinfo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackageDetailResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TenantPackageControllerTest {

    @Test
    void shouldReturnTenantPackagePage() throws Exception {
        TenantPackageService service = Mockito.mock(TenantPackageService.class);
        when(service.pageTenantPackages(any())).thenReturn(
            PageResult.of(List.of(new TenantPackagePageResponse(1L, "标准版", 0, 10, "default")), 1L)
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantPackageController(service)).build();

        mockMvc.perform(get("/admin/tenant-packages/page").param("packageName", "标准"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].packageName").value("标准版"));
    }

    @Test
    void shouldReturnTenantPackageDetail() throws Exception {
        TenantPackageService service = Mockito.mock(TenantPackageService.class);
        when(service.getTenantPackage(1L)).thenReturn(new TenantPackageDetailResponse(1L, "标准版", 0, 10, "default"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantPackageController(service)).build();

        mockMvc.perform(get("/admin/tenant-packages/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.packageName").value("标准版"));
    }

    @Test
    void shouldCreateTenantPackage() throws Exception {
        TenantPackageService service = Mockito.mock(TenantPackageService.class);
        when(service.createTenantPackage(any())).thenReturn(2L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantPackageController(service)).build();

        mockMvc.perform(post("/admin/tenant-packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"packageName":"专业版","status":0,"sort":20,"remark":"pro"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    void shouldUpdateTenantPackageStatus() throws Exception {
        TenantPackageService service = Mockito.mock(TenantPackageService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TenantPackageController(service)).build();

        mockMvc.perform(put("/admin/tenant-packages/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateTenantPackageStatus(eq(1L), any());
    }
}
