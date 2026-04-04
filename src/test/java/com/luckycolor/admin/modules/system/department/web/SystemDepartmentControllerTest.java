package com.luckycolor.admin.modules.system.department.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemDepartmentControllerTest {

    @Test
    void shouldReturnDepartmentTree() throws Exception {
        SystemDepartmentService service = Mockito.mock(SystemDepartmentService.class);
        when(service.listDepartmentTree(any())).thenReturn(List.of(
            new SystemDepartmentTreeResponse(
                1L,
                1L,
                0L,
                "Headquarters",
                "Alice",
                "13800000000",
                "hq@example.com",
                1,
                0,
                List.of(new SystemDepartmentTreeResponse(
                    2L,
                    1L,
                    1L,
                    "Engineering",
                    "Bob",
                    "13800000001",
                    "eng@example.com",
                    1,
                    0,
                    List.of()
                ))
            )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemDepartmentController(service)).build();

        mockMvc.perform(get("/admin/departments/tree").param("departmentName", "Head"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].departmentName").value("Headquarters"))
            .andExpect(jsonPath("$.data[0].children[0].departmentName").value("Engineering"));
    }

    @Test
    void shouldReturnDepartmentDetail() throws Exception {
        SystemDepartmentService service = Mockito.mock(SystemDepartmentService.class);
        when(service.getDepartment(1L)).thenReturn(new SystemDepartmentDetailResponse(
            1L,
            1L,
            0L,
            "Headquarters",
            "Alice",
            "13800000000",
            "hq@example.com",
            1,
            0,
            "default"
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemDepartmentController(service)).build();

        mockMvc.perform(get("/admin/departments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.departmentName").value("Headquarters"))
            .andExpect(jsonPath("$.data.leader").value("Alice"));
    }

    @Test
    void shouldCreateDepartment() throws Exception {
        SystemDepartmentService service = Mockito.mock(SystemDepartmentService.class);
        when(service.createDepartment(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemDepartmentController(service)).build();

        mockMvc.perform(post("/admin/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"parentId":0,"departmentName":"Engineering","leader":"Bob","phone":"13800000001","email":"eng@example.com","sort":1,"status":0,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateDepartmentStatus() throws Exception {
        SystemDepartmentService service = Mockito.mock(SystemDepartmentService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemDepartmentController(service)).build();

        mockMvc.perform(put("/admin/departments/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateDepartmentStatus(eq(1L), any());
    }

    @Test
    void shouldDeleteDepartment() throws Exception {
        SystemDepartmentService service = Mockito.mock(SystemDepartmentService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemDepartmentController(service)).build();

        mockMvc.perform(delete("/admin/departments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).deleteDepartment(1L);
    }
}
