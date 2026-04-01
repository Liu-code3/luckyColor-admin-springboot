package com.luckycolor.admin.modules.system.operationlog.web;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogService;
import com.luckycolor.admin.modules.system.operationlog.web.response.OperationLogPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OperationLogControllerTest {

    @Test
    void shouldReturnOperationLogPage() throws Exception {
        OperationLogService service = Mockito.mock(OperationLogService.class);
        Mockito.when(service.pageOperationLogs(any())).thenReturn(PageResult.of(List.of(
            new OperationLogPageResponse(
                1L,
                1L,
                1L,
                "admin",
                "users",
                "CREATE",
                "POST",
                "/admin/users",
                "username=admin",
                1,
                200,
                12L,
                "127.0.0.1",
                null,
                LocalDateTime.of(2026, 4, 1, 12, 0)
            )
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OperationLogController(service)).build();

        mockMvc.perform(get("/admin/operation-logs/page").param("bizModule", "users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].operationType").value("CREATE"));
    }
}
