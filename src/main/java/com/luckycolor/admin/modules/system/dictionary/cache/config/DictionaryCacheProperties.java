package com.luckycolor.admin.modules.system.dictionary.cache.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.dictionary")
public class DictionaryCacheProperties {

    private Duration cacheExpireIn = Duration.ofMinutes(30);
}
