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
    List<String> permissions,
    String dataScope,
    Long departmentId,
    List<Long> departmentIds,
    List<Long> scopeTenantIds
) {

    public AuthUser(
        Long userId,
        String username,
        String password,
        Long tenantId,
        String nickname,
        Integer status,
        List<String> roles,
        List<String> permissions
    ) {
        this(userId, username, password, tenantId, nickname, status, roles, permissions, "TENANT", null, List.of(), List.of());
    }
}
