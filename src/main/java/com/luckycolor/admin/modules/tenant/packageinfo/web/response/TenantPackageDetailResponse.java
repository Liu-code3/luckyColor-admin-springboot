package com.luckycolor.admin.modules.tenant.packageinfo.web.response;

public record TenantPackageDetailResponse(
    Long id,
    String packageName,
    Integer status,
    Integer sort,
    String remark
) {
}
