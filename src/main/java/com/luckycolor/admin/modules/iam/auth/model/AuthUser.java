package com.luckycolor.admin.modules.iam.auth.model;

import java.util.List;

public record AuthUser(
    Long userId,
    String username,
    String password,
    Long tenantId,
    String nickname,
    Integer status,
    List<String> roles,
    List<String> permissions
) {
}
