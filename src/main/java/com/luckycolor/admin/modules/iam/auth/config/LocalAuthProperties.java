package com.luckycolor.admin.modules.iam.auth.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.auth")
public class LocalAuthProperties {

    private List<User> localUsers = new ArrayList<>();

    @Getter
    @Setter
    public static class User {

        private Long userId;

        private String username;

        private String password;

        private Long tenantId;

        private String nickname;

        private Integer status = 0;

        private List<String> roles = new ArrayList<>();
    }
}
