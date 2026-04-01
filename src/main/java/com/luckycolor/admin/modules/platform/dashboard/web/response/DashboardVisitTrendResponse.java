package com.luckycolor.admin.modules.platform.dashboard.web.response;

import java.util.List;

public record DashboardVisitTrendResponse(
    List<DashboardVisitTrendPointResponse> points,
    long totalVisits,
    long successVisits,
    long failedVisits
) {
}
