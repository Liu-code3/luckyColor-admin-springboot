package com.luckycolor.admin.infrastructure.security.jwt;

import java.util.List;

public record JwtAccessTokenClaims(
    Long userId,
    String username,
    Long tenantId,
    List<String> roles
) {
}
