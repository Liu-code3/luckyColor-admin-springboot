package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.infrastructure.cache.core.CacheKeyBuilder;
import com.luckycolor.admin.infrastructure.cache.core.CacheKeyNames;
import com.luckycolor.admin.modules.iam.auth.service.AuthTokenSessionService;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnBean(RedisTemplate.class)
public class RedisAuthTokenSessionService implements AuthTokenSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyBuilder cacheKeyBuilder;

    public RedisAuthTokenSessionService(RedisTemplate<String, Object> redisTemplate, CacheKeyBuilder cacheKeyBuilder) {
        this.redisTemplate = redisTemplate;
        this.cacheKeyBuilder = cacheKeyBuilder;
    }

    @Override
    public void revoke(String token, Instant expiresAt) {
        if (token == null || expiresAt == null) {
            return;
        }
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redisTemplate.opsForValue().set(cacheKeyBuilder.build(CacheKeyNames.SECURITY_TOKEN, token), "revoked", ttl);
    }

    @Override
    public boolean isRevoked(String token) {
        if (token == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKeyBuilder.build(CacheKeyNames.SECURITY_TOKEN, token)));
    }
}
