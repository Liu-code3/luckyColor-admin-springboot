package com.luckycolor.admin.modules.platform.dashboard.web.response;

public record DashboardOverviewResponse(
    long activeTenantCount,
    long activeUserCount,
    long activeRoleCount,
    long activeMenuCount,
    long publishedNoticeCount,
    long loginTodayCount
) {
}
