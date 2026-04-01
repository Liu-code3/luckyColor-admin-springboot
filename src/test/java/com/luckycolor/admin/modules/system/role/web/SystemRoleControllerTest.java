package com.luckycolor.admin.modules.system.role.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.role.service.SystemRoleService;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleDetailResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRolePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemRoleControllerTest {

    @Test
    void shouldReturnRolePage() throws Exception {
        SystemRoleService service = Mockito.mock(SystemRoleService.class);
        when(service.pageRoles(any())).thenReturn(PageResult.of(List.of(
            new SystemRolePageResponse(1L, 1L, "ROLE_ADMIN", "Admin", 1, 0)
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(get("/admin/roles/page").param("roleCode", "ROLE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.list[0].roleCode").value("ROLE_ADMIN"));
    }

    @Test
    void shouldReturnRoleDetail() throws Exception {
        SystemRoleService service = Mockito.mock(SystemRoleService.class);
        when(service.getRole(1L)).thenReturn(new SystemRoleDetailResponse(1L, 1L, "ROLE_ADMIN", "Admin", 1, 0, "default"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(get("/admin/roles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleName").value("Admin"));
    }

    @Test
    void shouldCreateRole() throws Exception {
        SystemRoleService service = Mockito.mock(SystemRoleService.class);
        when(service.createRole(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(post("/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roleCode":"ROLE_ADMIN","roleName":"Admin","sort":1,"status":0,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateRoleStatus() throws Exception {
        SystemRoleService service = Mockito.mock(SystemRoleService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(put("/admin/roles/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateRoleStatus(eq(1L), any());
    }

    @Test
    void shouldReturnRoleAuthority() throws Exception {
        SystemRoleService service = Mockito.mock(SystemRoleService.class);
        when(service.getRoleAuthority(1L)).thenReturn(
            new SystemRoleAuthorityResponse(
                1L,
                1L,
                "ROLE_ADMIN",
                "Admin",
                List.of(1L, 2L),
                List.of("system:user:query"),
                "DEPARTMENT",
                100L,
                List.of(100L, 101L)
            )
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(get("/admin/roles/1/authority"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuIds[0]").value(1))
            .andExpect(jsonPath("$.data.dataScope").value("DEPARTMENT"));
    }

    @Test
    void shouldUpdateRoleAuthority() throws Exception {
        SystemRoleService service = Mockito.mock(SystemRoleService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(put("/admin/roles/1/authority")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"menuIds":[1,2],"permissionCodes":["system:user:query"],"dataScope":"DEPARTMENT","departmentId":100,"departmentIds":[100,101]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateRoleAuthority(eq(1L), any());
    }
}
