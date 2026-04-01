package com.luckycolor.admin.modules.system.config.web.response;

public record SystemConfigDetailResponse(
    Long id,
    Long tenantId,
    String configKey,
    String configName,
    String configValue,
    Integer sensitive,
    Integer status,
    Integer sort,
    String remark
) {
}
