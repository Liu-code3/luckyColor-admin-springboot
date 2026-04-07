package com.luckycolor.admin.modules.iam.auth.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.auth-anti-abuse")
public class AuthAntiAbuseProperties {

    private boolean enabled = true;

    private final Login login = new Login();

    private final RateLimit captcha = new RateLimit(Duration.ofMinutes(1), 20);

    private final RateLimit refresh = new RateLimit(Duration.ofMinutes(1), 30);

    @Getter
    @Setter
    public static class Login {

        private int maxFailures = 5;

        private Duration lockDuration = Duration.ofMinutes(15);
    }

    @Getter
    @Setter
    public static class RateLimit {

        private Duration window;

        private int maxRequests;

        public RateLimit() {
        }

        public RateLimit(Duration window, int maxRequests) {
            this.window = window;
            this.maxRequests = maxRequests;
        }
    }
}
