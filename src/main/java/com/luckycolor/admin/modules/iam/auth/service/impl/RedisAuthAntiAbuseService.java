package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.infrastructure.cache.core.CacheKeyBuilder;
import com.luckycolor.admin.infrastructure.cache.core.CacheKeyNames;
import com.luckycolor.admin.modules.iam.auth.config.AuthAntiAbuseProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthAntiAbuseService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Primary
@ConditionalOnBean(RedisTemplate.class)
public class RedisAuthAntiAbuseService implements AuthAntiAbuseService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyBuilder cacheKeyBuilder;
    private final AuthAntiAbuseProperties properties;

    public RedisAuthAntiAbuseService(
        RedisTemplate<String, Object> redisTemplate,
        CacheKeyBuilder cacheKeyBuilder,
        AuthAntiAbuseProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.cacheKeyBuilder = cacheKeyBuilder;
        this.properties = properties;
    }

    @Override
    public void checkLoginAllowed(String tenantId, String username) {
        if (!properties.isEnabled() || !StringUtils.hasText(username)) {
            return;
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(loginLockKey(tenantId, username)))) {
            throw tooManyRequests("AUTH_LOGIN_LOCKED");
        }
    }

    @Override
    public void recordLoginFailure(String tenantId, String username) {
        if (!properties.isEnabled() || !StringUtils.hasText(username)) {
            return;
        }
        String countKey = loginCountKey(tenantId, username);
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count == null) {
            return;
        }
        if (count == 1L) {
            redisTemplate.expire(countKey, properties.getLogin().getLockDuration());
        }
        if (count < properties.getLogin().getMaxFailures()) {
            return;
        }
        redisTemplate.opsForValue().set(loginLockKey(tenantId, username), "locked", properties.getLogin().getLockDuration());
        redisTemplate.delete(countKey);
    }

    @Override
    public void clearLoginFailures(String tenantId, String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        redisTemplate.delete(loginCountKey(tenantId, username));
        redisTemplate.delete(loginLockKey(tenantId, username));
    }

    @Override
    public void checkCaptchaAllowed(String remoteIp) {
        enforceRateLimit(captchaKey(remoteIp), properties.getCaptcha(), "AUTH_CAPTCHA_RATE_LIMITED");
    }

    @Override
    public void checkRefreshAllowed(String refreshToken) {
        if (!properties.isEnabled() || !StringUtils.hasText(refreshToken)) {
            return;
        }
        enforceRateLimit(refreshKey(refreshToken), properties.getRefresh(), "AUTH_REFRESH_RATE_LIMITED");
    }

    private void enforceRateLimit(String key, AuthAntiAbuseProperties.RateLimit rateLimit, String reason) {
        if (!properties.isEnabled()) {
            return;
        }
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return;
        }
        if (count == 1L) {
            redisTemplate.expire(key, rateLimit.getWindow());
        }
        if (count > rateLimit.getMaxRequests()) {
            throw tooManyRequests(reason);
        }
    }

    private String loginCountKey(String tenantId, String username) {
        return cacheKeyBuilder.build(CacheKeyNames.AUTH_ANTI_ABUSE, "login", "count", normalizeSegment(tenantId), normalizeSegment(username));
    }

    private String loginLockKey(String tenantId, String username) {
        return cacheKeyBuilder.build(CacheKeyNames.AUTH_ANTI_ABUSE, "login", "lock", normalizeSegment(tenantId), normalizeSegment(username));
    }

    private String captchaKey(String remoteIp) {
        return cacheKeyBuilder.build(CacheKeyNames.AUTH_ANTI_ABUSE, "captcha", normalizeSegment(remoteIp));
    }

    private String refreshKey(String refreshToken) {
        return cacheKeyBuilder.build(CacheKeyNames.AUTH_ANTI_ABUSE, "refresh", hashToken(refreshToken));
    }

    private String normalizeSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "default";
        }
        return value.trim().toLowerCase();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private ResponseStatusException tooManyRequests(String reason) {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, reason);
    }
}
