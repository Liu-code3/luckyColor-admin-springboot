package com.luckycolor.admin.infrastructure.security.jwt;

public record JwtAuthenticatedUser(
    Long userId,
    String username,
    Long tenantId
) {
}
