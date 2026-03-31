package com.luckycolor.admin.infrastructure.security.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationStyle;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.jwt")
public class SecurityJwtProperties {

    private String secret = "replace-with-a-strong-secret-for-luckycolor-admin";

    private String expiresIn = "2h";

    public Duration resolveExpiresIn() {
        return DurationStyle.detectAndParse(expiresIn);
    }
}
