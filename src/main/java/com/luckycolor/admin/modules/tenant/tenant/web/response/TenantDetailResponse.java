package com.luckycolor.admin.modules.tenant.tenant.web.response;

import java.time.LocalDateTime;

public record TenantDetailResponse(
    Long id,
    String name,
    Long packageId,
    String contactName,
    String contactMobile,
    Integer accountCount,
    LocalDateTime expireTime,
    Integer status
) {
}
