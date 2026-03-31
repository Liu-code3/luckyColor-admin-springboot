package com.luckycolor.admin.infrastructure.cache.core;

import com.luckycolor.admin.infrastructure.cache.config.CacheProperties;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CacheKeyBuilder {

    private static final String SEPARATOR = ":";

    private final CacheProperties cacheProperties;

    public CacheKeyBuilder(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    public String build(String namespace, Object... segments) {
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        return Stream.concat(
                Stream.of(cacheProperties.getKeyPrefix(), namespace),
                Arrays.stream(segments == null ? new Object[0] : segments)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .filter(StringUtils::hasText)
            )
            .reduce((left, right) -> left + SEPARATOR + right)
            .orElseThrow();
    }
}
