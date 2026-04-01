package com.luckycolor.admin.modules.platform.codegen.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.codegen.service.CodegenMetadataService;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenColumnResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenDiscoveryTableResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTableDetailResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTablePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CodegenMetadataControllerTest {

    @Test
    void shouldReturnDiscoveryPage() throws Exception {
        CodegenMetadataService service = Mockito.mock(CodegenMetadataService.class);
        Mockito.when(service.pageDiscoveryTables(any())).thenReturn(PageResult.of(List.of(
            new CodegenDiscoveryTableResponse("sys_user", "System User")
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CodegenMetadataController(service)).build();

        mockMvc.perform(get("/admin/codegen/tables/discovery").param("tableName", "user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].tableName").value("sys_user"));
    }

    @Test
    void shouldImportTables() throws Exception {
        CodegenMetadataService service = Mockito.mock(CodegenMetadataService.class);
        Mockito.when(service.importTables(eq(1L), any())).thenReturn(1);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CodegenMetadataController(service)).build();

        mockMvc.perform(post("/admin/codegen/tables/import")
                .principal(authentication())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tableNames":["sys_user"]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldReturnImportedPage() throws Exception {
        CodegenMetadataService service = Mockito.mock(CodegenMetadataService.class);
        Mockito.when(service.pageTables(any())).thenReturn(PageResult.of(List.of(
            new CodegenTablePageResponse(
                1L, 1L, "sys_user", "System User", "user", "User", "system",
                "com.luckycolor.admin.modules.generated", "crud", 5, "default"
            )
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CodegenMetadataController(service)).build();

        mockMvc.perform(get("/admin/codegen/tables/page"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].className").value("User"));
    }

    @Test
    void shouldReturnDetail() throws Exception {
        CodegenMetadataService service = Mockito.mock(CodegenMetadataService.class);
        Mockito.when(service.getTable(1L)).thenReturn(new CodegenTableDetailResponse(
            1L,
            1L,
            "sys_user",
            "System User",
            "user",
            "User",
            "system",
            "com.luckycolor.admin.modules.generated",
            "crud",
            1,
            "default",
            List.of(new CodegenColumnResponse(11L, "user_name", "User Name", "varchar", "String", "userName", "input", "LIKE", 1, 1, 1, 0))
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CodegenMetadataController(service)).build();

        mockMvc.perform(get("/admin/codegen/tables/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.columns[0].columnName").value("user_name"));
    }

    @Test
    void shouldUpdateTable() throws Exception {
        CodegenMetadataService service = Mockito.mock(CodegenMetadataService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CodegenMetadataController(service)).build();

        mockMvc.perform(put("/admin/codegen/tables/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"businessName":"user","className":"User","moduleName":"system","packageName":"com.luckycolor.admin.modules.generated","genMode":"crud","remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldUpdateColumns() throws Exception {
        CodegenMetadataService service = Mockito.mock(CodegenMetadataService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CodegenMetadataController(service)).build();

        mockMvc.perform(put("/admin/codegen/tables/1/columns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"columns":[{"id":11,"javaType":"String","javaField":"userName","htmlType":"input","queryType":"LIKE","required":1,"listVisible":1,"formVisible":1,"status":0}]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(
            new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN")),
            null
        );
    }
}
