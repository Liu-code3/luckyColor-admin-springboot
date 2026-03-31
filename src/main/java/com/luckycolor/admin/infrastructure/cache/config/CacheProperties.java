package com.luckycolor.admin.infrastructure.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private String keyPrefix = "luckycolor-admin";
}
