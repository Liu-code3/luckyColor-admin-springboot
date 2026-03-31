package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.service.AuthTokenSessionService;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(RedisTemplate.class)
public class InMemoryAuthTokenSessionService implements AuthTokenSessionService {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    @Override
    public void revoke(String token, Instant expiresAt) {
        if (token == null || expiresAt == null) {
            return;
        }
        revokedTokens.put(token, expiresAt);
    }

    @Override
    public boolean isRevoked(String token) {
        if (token == null) {
            return false;
        }
        clearExpiredTokens();
        Instant expiresAt = revokedTokens.get(token);
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    private void clearExpiredTokens() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
