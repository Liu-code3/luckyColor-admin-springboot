package com.luckycolor.admin.modules.platform.dashboard.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.platform.dashboard.service.DashboardService;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardOverviewResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardRecentVisitResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendPointResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerTest {

    @Test
    void shouldReturnOverview() throws Exception {
        DashboardService service = Mockito.mock(DashboardService.class);
        when(service.getOverview()).thenReturn(new DashboardOverviewResponse(3L, 12L, 5L, 22L, 4L, 18L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mockMvc.perform(get("/admin/dashboard/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.activeTenantCount").value(3))
            .andExpect(jsonPath("$.data.loginTodayCount").value(18));
    }

    @Test
    void shouldReturnVisitTrend() throws Exception {
        DashboardService service = Mockito.mock(DashboardService.class);
        when(service.getVisitTrend()).thenReturn(new DashboardVisitTrendResponse(
            List.of(new DashboardVisitTrendPointResponse(LocalDate.of(2026, 4, 1), 3L, 2L, 1L)),
            3L,
            2L,
            1L
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mockMvc.perform(get("/admin/dashboard/visit-trend"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalVisits").value(3))
            .andExpect(jsonPath("$.data.points[0].successCount").value(2));
    }

    @Test
    void shouldReturnRecentVisits() throws Exception {
        DashboardService service = Mockito.mock(DashboardService.class);
        when(service.listRecentVisits()).thenReturn(List.of(
            new DashboardRecentVisitResponse(1L, 1L, 1L, "admin", "/auth/login", "127.0.0.1", LocalDateTime.of(2026, 4, 1, 9, 0))
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mockMvc.perform(get("/admin/dashboard/recent-visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].username").value("admin"))
            .andExpect(jsonPath("$.data[0].requestUri").value("/auth/login"));
    }
}
