package com.luckycolor.admin.modules.platform.dashboard.service;

import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardOverviewResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardRecentVisitResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendResponse;
import java.util.List;

public interface DashboardService {

    DashboardOverviewResponse getOverview();

    DashboardVisitTrendResponse getVisitTrend();

    List<DashboardRecentVisitResponse> listRecentVisits();
}
