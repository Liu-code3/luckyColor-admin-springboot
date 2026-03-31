package com.luckycolor.admin.modules.iam.auth.web.response;

import java.util.List;

public record AuthAccessSnapshotResponse(
    Long userId,
    Long tenantId,
    List<String> roles,
    List<String> permissions,
    List<String> routeCodes,
    String homePath
) {
}
