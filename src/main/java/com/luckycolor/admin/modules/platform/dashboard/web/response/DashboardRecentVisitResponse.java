package com.luckycolor.admin.modules.platform.dashboard.web.response;

import java.time.LocalDateTime;

public record DashboardRecentVisitResponse(
    Long id,
    Long tenantId,
    Long userId,
    String username,
    String requestUri,
    String remoteIp,
    LocalDateTime accessTime
) {
}
