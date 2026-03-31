package com.luckycolor.admin.modules.tenant.audit.web.response;

import java.time.LocalDateTime;

public record TenantAuditLogPageResponse(
    Long id,
    Long tenantId,
    String targetType,
    Long targetId,
    String action,
    String content,
    LocalDateTime createTime
) {
}
