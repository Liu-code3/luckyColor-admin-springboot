package com.luckycolor.admin.modules.iam.auth.web.response;

import java.util.List;

public record AuthLoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    Long userId,
    String username,
    String nickname,
    Long tenantId,
    List<String> roles
) {
}
