package com.luckycolor.admin.modules.system.role.web.response;

public record SystemRoleDetailResponse(
    Long id,
    Long tenantId,
    String roleCode,
    String roleName,
    Integer sort,
    Integer status,
    String remark
) {
}
