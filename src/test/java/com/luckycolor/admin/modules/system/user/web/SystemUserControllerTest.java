package com.luckycolor.admin.modules.system.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemUserControllerTest {

    @Test
    void shouldReturnUserPage() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.pageUsers(any())).thenReturn(PageResult.of(List.of(
            new SystemUserPageResponse(
                1L,
                1L,
                "admin",
                "System Admin",
                "admin@example.com",
                "13800000000",
                100L,
                List.of("ROLE_SUPER_ADMIN"),
                0
            )
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(get("/admin/users/page").param("username", "admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.list[0].username").value("admin"));
    }

    @Test
    void shouldReturnUserDetail() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.getUser(1L)).thenReturn(new SystemUserDetailResponse(
            1L,
            1L,
            "admin",
            "System Admin",
            "admin@example.com",
            "13800000000",
            100L,
            List.of("ROLE_SUPER_ADMIN"),
            List.of("system:user:query"),
            "ALL",
            List.of(100L, 101L),
            List.of(1L),
            0,
            "default"
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(get("/admin/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("ROLE_SUPER_ADMIN"));
    }

    @Test
    void shouldReturnRoleOptions() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.listRoleOptions()).thenReturn(List.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(get("/admin/users/role-options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0]").value("ROLE_SUPER_ADMIN"));
    }

    @Test
    void shouldReturnExportPreview() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.listUsersForExportPreview(any())).thenReturn(List.of(
            new SystemUserExportPreviewResponse(
                1L,
                "admin",
                "System Admin",
                "admin@example.com",
                "13800000000",
                List.of("ROLE_SUPER_ADMIN"),
                0
            )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(get("/admin/users/export-preview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.createUser(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin123","nickname":"System Admin","email":"admin@example.com","mobile":"13800000000","roleCodes":["ROLE_SUPER_ADMIN"],"permissionCodes":["system:user:query"],"dataScope":"ALL","departmentId":100,"departmentIds":[100,101],"scopeTenantIds":[1],"status":0,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateUserStatus() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(put("/admin/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateUserStatus(eq(1L), any());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(delete("/admin/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).deleteUser(1L);
    }

    @Test
    void shouldResetPassword() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(put("/admin/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"newPassword":"new-password"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).resetPassword(eq(1L), any());
    }

    @Test
    void shouldAssignRoles() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(put("/admin/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roleCodes":["ROLE_ADMIN","ROLE_EDITOR"]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).assignRoles(eq(1L), any());
    }

    @Test
    void shouldExportUsers() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.exportUsers(any())).thenReturn("username,nickname\nadmin,System Admin\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();

        mockMvc.perform(get("/admin/users/export"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldImportUsers() throws Exception {
        SystemUserService service = Mockito.mock(SystemUserService.class);
        when(service.importUsers(any())).thenReturn(1);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemUserController(service)).build();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "users.csv",
            "text/csv",
            "username,nickname\nadmin,System Admin\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/admin/users/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }
}
