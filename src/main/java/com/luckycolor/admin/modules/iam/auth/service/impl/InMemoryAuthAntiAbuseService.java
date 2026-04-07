package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.config.AuthAntiAbuseProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthAntiAbuseService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnMissingBean(RedisTemplate.class)
public class InMemoryAuthAntiAbuseService implements AuthAntiAbuseService {

    private final AuthAntiAbuseProperties properties;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final Map<String, Instant> loginLocks = new ConcurrentHashMap<>();

    public InMemoryAuthAntiAbuseService(AuthAntiAbuseProperties properties) {
        this.properties = properties;
    }

    @Override
    public void checkLoginAllowed(String tenantId, String username) {
        if (!properties.isEnabled() || !StringUtils.hasText(username)) {
            return;
        }
        String key = loginKey(tenantId, username);
        Instant lockedUntil = loginLocks.get(key);
        if (lockedUntil == null) {
            return;
        }
        if (!lockedUntil.isAfter(Instant.now())) {
            loginLocks.remove(key, lockedUntil);
            return;
        }
        throw tooManyRequests("AUTH_LOGIN_LOCKED");
    }

    @Override
    public void recordLoginFailure(String tenantId, String username) {
        if (!properties.isEnabled() || !StringUtils.hasText(username)) {
            return;
        }
        Instant now = Instant.now();
        String key = loginKey(tenantId, username);
        WindowCounter counter = counters.compute(key, (ignored, current) -> nextCounter(current, now, properties.getLogin().getLockDuration()));
        if (counter.count() < properties.getLogin().getMaxFailures()) {
            return;
        }
        counters.remove(key);
        loginLocks.put(key, now.plus(properties.getLogin().getLockDuration()));
    }

    @Override
    public void clearLoginFailures(String tenantId, String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        String key = loginKey(tenantId, username);
        counters.remove(key);
        loginLocks.remove(key);
    }

    @Override
    public void checkCaptchaAllowed(String remoteIp) {
        enforceRateLimit("captcha:" + normalizeSegment(remoteIp), properties.getCaptcha(), "AUTH_CAPTCHA_RATE_LIMITED");
    }

    @Override
    public void checkRefreshAllowed(String refreshToken) {
        if (!properties.isEnabled() || !StringUtils.hasText(refreshToken)) {
            return;
        }
        enforceRateLimit("refresh:" + hashToken(refreshToken), properties.getRefresh(), "AUTH_REFRESH_RATE_LIMITED");
    }

    private void enforceRateLimit(String key, AuthAntiAbuseProperties.RateLimit rateLimit, String reason) {
        if (!properties.isEnabled()) {
            return;
        }
        Instant now = Instant.now();
        WindowCounter counter = counters.compute(key, (ignored, current) -> nextCounter(current, now, rateLimit.getWindow()));
        if (counter.count() > rateLimit.getMaxRequests()) {
            throw tooManyRequests(reason);
        }
    }

    private WindowCounter nextCounter(WindowCounter current, Instant now, Duration window) {
        if (current == null || !current.expiresAt().isAfter(now)) {
            return new WindowCounter(1, now.plus(window));
        }
        return new WindowCounter(current.count() + 1, current.expiresAt());
    }

    private String loginKey(String tenantId, String username) {
        return "login:" + normalizeSegment(tenantId) + ':' + normalizeSegment(username);
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

    private record WindowCounter(int count, Instant expiresAt) {
    }
}
