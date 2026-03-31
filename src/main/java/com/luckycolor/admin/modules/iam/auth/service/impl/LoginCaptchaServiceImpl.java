package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.infrastructure.cache.core.CacheKeyBuilder;
import com.luckycolor.admin.infrastructure.cache.core.CacheKeyNames;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnProperty(prefix = "app.login-captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoginCaptchaServiceImpl implements LoginCaptchaService {

    private static final String CAPTCHA_CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyBuilder cacheKeyBuilder;
    private final LoginCaptchaProperties loginCaptchaProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public LoginCaptchaServiceImpl(
        RedisTemplate<String, Object> redisTemplate,
        CacheKeyBuilder cacheKeyBuilder,
        LoginCaptchaProperties loginCaptchaProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.cacheKeyBuilder = cacheKeyBuilder;
        this.loginCaptchaProperties = loginCaptchaProperties;
    }

    @Override
    public LoginCaptchaResponse createCaptcha() {
        String captchaCode = generateCaptchaCode();
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
            cacheKeyBuilder.build(CacheKeyNames.LOGIN_CAPTCHA, captchaKey),
            captchaCode,
            loginCaptchaProperties.getExpireIn()
        );
        return new LoginCaptchaResponse(captchaKey, buildCaptchaImage(captchaCode));
    }

    @Override
    public void validateCaptcha(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha is required");
        }
        String cacheKey = cacheKeyBuilder.build(CacheKeyNames.LOGIN_CAPTCHA, captchaKey);
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        redisTemplate.delete(cacheKey);
        if (!(cachedValue instanceof String cachedCaptcha) || !StringUtils.hasText(cachedCaptcha)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha has expired");
        }
        if (!cachedCaptcha.equalsIgnoreCase(captchaCode.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha is invalid");
        }
    }

    private String generateCaptchaCode() {
        StringBuilder builder = new StringBuilder(loginCaptchaProperties.getCodeLength());
        for (int i = 0; i < loginCaptchaProperties.getCodeLength(); i++) {
            int index = secureRandom.nextInt(CAPTCHA_CHARSET.length());
            builder.append(CAPTCHA_CHARSET.charAt(index));
        }
        return builder.toString();
    }

    private String buildCaptchaImage(String captchaCode) {
        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d">
              <rect width="100%%" height="100%%" rx="8" fill="#f7f4ea"/>
              <path d="M8 10 L112 34" stroke="#d6c6a5" stroke-width="2" />
              <path d="M14 34 L106 12" stroke="#8aa0b8" stroke-width="2" />
              <circle cx="18" cy="21" r="3" fill="#d38b5d" opacity="0.55" />
              <circle cx="102" cy="27" r="4" fill="#7aa37a" opacity="0.45" />
              <text x="50%%" y="58%%" text-anchor="middle" dominant-baseline="middle"
                    font-family="monospace" font-size="24" letter-spacing="6"
                    fill="#2b3440">%s</text>
            </svg>
            """.formatted(
            loginCaptchaProperties.getWidth(),
            loginCaptchaProperties.getHeight(),
            loginCaptchaProperties.getWidth(),
            loginCaptchaProperties.getHeight(),
            captchaCode
        );
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }
}
