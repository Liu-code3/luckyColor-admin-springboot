package com.luckycolor.admin.infrastructure.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    @Test
    void shouldCreateAndParseAccessToken() {
        SecurityJwtProperties properties = new SecurityJwtProperties();
        properties.setSecret("test-secret-for-jwt-token-service-luckycolor");
        properties.setExpiresIn("2h");
        JwtTokenService jwtTokenService = new JwtTokenService(properties);

        String token = jwtTokenService.createAccessToken(1L, "coderLiu", 1001L, List.of("ROLE_ADMIN"));
        JwtAccessTokenClaims claims = jwtTokenService.parseAccessToken(token);

        assertThat(claims.userId()).isEqualTo(1L);
        assertThat(claims.username()).isEqualTo("coderLiu");
        assertThat(claims.tenantId()).isEqualTo(1001L);
        assertThat(claims.roles()).containsExactly("ROLE_ADMIN");
    }
}
