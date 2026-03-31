package com.luckycolor.admin.modules.iam.auth.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.login-captcha")
public class LoginCaptchaProperties {

    private boolean enabled = true;

    private Duration expireIn = Duration.ofMinutes(2);

    private int width = 120;

    private int height = 42;

    private int codeLength = 4;
}
