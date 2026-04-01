package com.luckycolor.admin.modules.system.department.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
            .andExpect(jsonPath("$.code").value(0))
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
}
