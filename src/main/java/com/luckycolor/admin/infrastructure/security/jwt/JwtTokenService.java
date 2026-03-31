package com.luckycolor.admin.infrastructure.security.jwt;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenService {

    private static final String CLAIM_USER_ID = "userId";

    private static final String CLAIM_TENANT_ID = "tenantId";

    private static final String CLAIM_ROLES = "roles";

    private final SecurityJwtProperties jwtProperties;

    public JwtTokenService(SecurityJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createAccessToken(Long userId, String username, Long tenantId, List<String> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.resolveExpiresIn());

        return Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_TENANT_ID, tenantId)
            .claim(CLAIM_ROLES, roles == null ? Collections.emptyList() : roles)
            .signWith(buildSecretKey())
            .compact();
    }

    public JwtAccessTokenClaims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(buildSecretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return new JwtAccessTokenClaims(
            claims.get(CLAIM_USER_ID, Long.class),
            claims.getSubject(),
            claims.get(CLAIM_TENANT_ID, Long.class),
            resolveRoles(claims.get(CLAIM_ROLES))
        );
    }

    public String resolveBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        String value = authorizationHeader.trim();
        if (!value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = value.substring(7).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    private SecretKey buildSecretKey() {
        return Keys.hmacShaKeyFor(hashSecret(jwtProperties.getSecret()));
    }

    private List<String> resolveRoles(Object rolesClaim) {
        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .toList();
    }

    private byte[] hashSecret(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
