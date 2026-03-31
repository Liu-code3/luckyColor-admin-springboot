package com.luckycolor.admin.modules.system.user.web.response;

import java.util.List;

public record SystemUserDetailResponse(
    Long id,
    Long tenantId,
    String username,
    String nickname,
    String email,
    String mobile,
    Long departmentId,
    List<String> roleCodes,
    List<String> permissionCodes,
    String dataScope,
    List<Long> departmentIds,
    List<Long> scopeTenantIds,
    Integer status,
    String remark
) {
}
