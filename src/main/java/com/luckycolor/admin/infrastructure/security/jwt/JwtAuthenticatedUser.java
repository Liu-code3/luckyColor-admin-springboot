package com.luckycolor.admin.infrastructure.security.jwt;

import java.util.List;

public record JwtAuthenticatedUser(
    Long userId,
    String username,
    Long tenantId,
    List<String> roles
) {
}
