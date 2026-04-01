package com.luckycolor.admin.modules.system.role.web.response;

public record SystemRolePageResponse(
    Long id,
    Long tenantId,
    String roleCode,
    String roleName,
    Integer sort,
    Integer status
) {
}
