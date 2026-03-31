package com.luckycolor.admin.modules.system.user.web.response;

import java.util.List;

public record SystemUserPageResponse(
    Long id,
    Long tenantId,
    String username,
    String nickname,
    String email,
    String mobile,
    Long departmentId,
    List<String> roleCodes,
    Integer status
) {
}
