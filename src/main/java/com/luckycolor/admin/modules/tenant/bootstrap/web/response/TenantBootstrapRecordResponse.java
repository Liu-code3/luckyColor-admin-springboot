package com.luckycolor.admin.modules.tenant.bootstrap.web.response;

import java.time.LocalDateTime;
import java.util.List;

public record TenantBootstrapRecordResponse(
    Long id,
    Long tenantId,
    String templateCode,
    String templateName,
    List<String> roleCodes,
    List<String> menuCodes,
    String adminUsername,
    String adminNickname,
    Integer status,
    LocalDateTime bootstrapTime,
    String remark
) {
}
