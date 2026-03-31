package com.luckycolor.admin.modules.iam.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.cache.config.CacheProperties;
import com.luckycolor.admin.infrastructure.cache.core.CacheKeyBuilder;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.impl.LoginCaptchaServiceImpl;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.server.ResponseStatusException;

class LoginCaptchaServiceImplTest {

    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateCaptchaAndCacheCode() {
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        LoginCaptchaService service = new LoginCaptchaServiceImpl(redisTemplate, buildCacheKeyBuilder(), buildProperties());

        LoginCaptchaResponse response = service.createCaptcha();

        assertThat(response.captchaKey()).isNotBlank();
        assertThat(response.captchaImage()).startsWith("data:image/svg+xml;base64,");
        verify(valueOperations).set(any(String.class), any(String.class), eq(Duration.ofMinutes(2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldValidateCaptchaAndDeleteCache() {
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("luckycolor-admin:login:captcha:key-1")).thenReturn("ABCD");
        LoginCaptchaService service = new LoginCaptchaServiceImpl(redisTemplate, buildCacheKeyBuilder(), buildProperties());

        service.validateCaptcha("key-1", "abcd");

        verify(redisTemplate).delete("luckycolor-admin:login:captcha:key-1");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldRejectInvalidCaptcha() {
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("luckycolor-admin:login:captcha:key-2")).thenReturn("ABCD");
        LoginCaptchaService service = new LoginCaptchaServiceImpl(redisTemplate, buildCacheKeyBuilder(), buildProperties());

        assertThatThrownBy(() -> service.validateCaptcha("key-2", "ZZZZ"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        verify(redisTemplate).delete("luckycolor-admin:login:captcha:key-2");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldRejectExpiredCaptcha() {
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("luckycolor-admin:login:captcha:key-3")).thenReturn(null);
        LoginCaptchaService service = new LoginCaptchaServiceImpl(redisTemplate, buildCacheKeyBuilder(), buildProperties());

        assertThatThrownBy(() -> service.validateCaptcha("key-3", "ABCD"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        verify(redisTemplate).delete("luckycolor-admin:login:captcha:key-3");
    }

    private CacheKeyBuilder buildCacheKeyBuilder() {
        CacheProperties cacheProperties = new CacheProperties();
        cacheProperties.setKeyPrefix("luckycolor-admin");
        return new CacheKeyBuilder(cacheProperties);
    }

    private LoginCaptchaProperties buildProperties() {
        LoginCaptchaProperties properties = new LoginCaptchaProperties();
        properties.setExpireIn(Duration.ofMinutes(2));
        properties.setWidth(120);
        properties.setHeight(42);
        properties.setCodeLength(4);
        return properties;
    }
}
