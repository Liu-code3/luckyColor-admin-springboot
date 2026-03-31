package com.luckycolor.admin.modules.iam.audit.web.response;

import java.time.LocalDateTime;

public record SecurityAuditLogPageResponse(
    Long id,
    Long tenantId,
    Long userId,
    String username,
    String eventType,
    Integer success,
    String requestMethod,
    String requestUri,
    String remoteIp,
    String reason,
    LocalDateTime createTime
) {
}
