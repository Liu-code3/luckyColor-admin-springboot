package com.luckycolor.admin.modules.system.role.web.response;

import java.util.List;

public record SystemRoleAuthorityResponse(
    Long id,
    Long tenantId,
    String roleCode,
    String roleName,
    List<Long> menuIds,
    List<String> permissionCodes,
    String dataScope,
    Long departmentId,
    List<Long> departmentIds
) {
}
