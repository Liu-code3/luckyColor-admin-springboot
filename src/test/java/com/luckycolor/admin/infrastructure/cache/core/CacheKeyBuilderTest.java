package com.luckycolor.admin.infrastructure.cache.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.luckycolor.admin.infrastructure.cache.config.CacheProperties;
import org.junit.jupiter.api.Test;

class CacheKeyBuilderTest {

    @Test
    void shouldBuildKeyWithPrefixAndSegments() {
        CacheProperties properties = new CacheProperties();
        properties.setKeyPrefix("luckycolor-admin");
        CacheKeyBuilder cacheKeyBuilder = new CacheKeyBuilder(properties);

        String key = cacheKeyBuilder.build(CacheKeyNames.LOGIN_CAPTCHA, "tenant-1", "code-01");

        assertThat(key).isEqualTo("luckycolor-admin:login:captcha:tenant-1:code-01");
    }

    @Test
    void shouldIgnoreBlankSegments() {
        CacheProperties properties = new CacheProperties();
        CacheKeyBuilder cacheKeyBuilder = new CacheKeyBuilder(properties);

        String key = cacheKeyBuilder.build(CacheKeyNames.SECURITY_TOKEN, "", null, "jwt-1");

        assertThat(key).isEqualTo("luckycolor-admin:security:token:jwt-1");
    }

    @Test
    void shouldRejectBlankNamespace() {
        CacheProperties properties = new CacheProperties();
        CacheKeyBuilder cacheKeyBuilder = new CacheKeyBuilder(properties);

        assertThatThrownBy(() -> cacheKeyBuilder.build(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("namespace");
    }
}
