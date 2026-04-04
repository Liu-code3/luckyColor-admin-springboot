package com.luckycolor.admin.infrastructure.security.jwt;

public record JwtRefreshTokenClaims(
    Long userId,
    String username,
    Long tenantId
) {
}
