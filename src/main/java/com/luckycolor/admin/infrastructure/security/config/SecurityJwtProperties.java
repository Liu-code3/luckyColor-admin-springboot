package com.luckycolor.admin.infrastructure.security.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.jwt")
public class SecurityJwtProperties {

    private String secret = "replace-with-a-strong-secret-for-luckycolor-admin";

    private String expiresIn = "2h";

    private String refreshSecret;

    private String refreshExpiresIn = "7d";

    private String refreshCookieName = "lc_refresh_token";

    private String refreshCookiePath = "/";

    private boolean refreshCookieSecure;

    public Duration resolveExpiresIn() {
        return DurationStyle.detectAndParse(expiresIn);
    }

    public String resolveRefreshSecret() {
        return StringUtils.hasText(refreshSecret) ? refreshSecret.trim() : secret;
    }

    public Duration resolveRefreshExpiresIn() {
        return DurationStyle.detectAndParse(refreshExpiresIn);
    }
}
