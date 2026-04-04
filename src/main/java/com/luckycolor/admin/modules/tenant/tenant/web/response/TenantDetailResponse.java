package com.luckycolor.admin.modules.tenant.tenant.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Tenant detail")
public record TenantDetailResponse(
    @Schema(description = "Tenant ID", example = "1")
    Long id,
    @Schema(description = "Tenant name", example = "LuckyColor Demo")
    String name,
    @Schema(description = "Tenant package ID", example = "10")
    Long packageId,
    @Schema(description = "Contact name", example = "张三")
    String contactName,
    @Schema(description = "Contact mobile", example = "13800138000")
    String contactMobile,
    @Schema(description = "Allowed account count", example = "100")
    Integer accountCount,
    @Schema(description = "Expiration time", example = "2026-12-31T23:59:59")
    LocalDateTime expireTime,
    @Schema(description = "Status: 0 enabled, 1 disabled", example = "0")
    Integer status
) {
}
