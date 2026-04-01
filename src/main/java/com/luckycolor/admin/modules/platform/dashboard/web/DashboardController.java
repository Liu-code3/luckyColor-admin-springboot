package com.luckycolor.admin.modules.platform.dashboard.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.platform.dashboard.service.DashboardService;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardOverviewResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardRecentVisitResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@ConditionalOnBean(DashboardService.class)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @RequirePermission("dashboard:query")
    public ApiResponse<DashboardOverviewResponse> overview() {
        return ApiResponse.success(dashboardService.getOverview());
    }

    @GetMapping("/visit-trend")
    @RequirePermission("dashboard:query")
    public ApiResponse<DashboardVisitTrendResponse> visitTrend() {
        return ApiResponse.success(dashboardService.getVisitTrend());
    }

    @GetMapping("/recent-visits")
    @RequirePermission("dashboard:query")
    public ApiResponse<List<DashboardRecentVisitResponse>> recentVisits() {
        return ApiResponse.success(dashboardService.listRecentVisits());
    }
}
