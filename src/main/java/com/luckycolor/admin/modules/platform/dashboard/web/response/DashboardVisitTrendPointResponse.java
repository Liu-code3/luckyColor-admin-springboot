package com.luckycolor.admin.modules.platform.dashboard.web.response;

import java.time.LocalDate;

public record DashboardVisitTrendPointResponse(
    LocalDate date,
    long visitCount,
    long successCount,
    long failureCount
) {
}
