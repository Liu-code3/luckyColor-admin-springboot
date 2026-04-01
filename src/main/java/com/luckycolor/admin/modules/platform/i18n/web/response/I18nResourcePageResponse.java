package com.luckycolor.admin.modules.platform.i18n.web.response;

public record I18nResourcePageResponse(
    Long id,
    Long tenantId,
    String locale,
    String namespace,
    String resourceKey,
    String resourceValue,
    Integer version,
    Integer status,
    String remark
) {
}
