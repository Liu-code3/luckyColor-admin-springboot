package com.luckycolor.admin.modules.iam.auth.web.response;

import java.util.List;

public record AuthProfileResponse(
    Long userId,
    String username,
    String nickname,
    Long tenantId,
    List<String> roles
) {
}
