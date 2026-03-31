package com.luckycolor.admin.modules.iam.auth.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.access")
public class AuthAccessProperties {

    private List<Route> routes = new ArrayList<>();

    @Getter
    @Setter
    public static class Route {

        private String code;

        private String name;

        private String path;

        private String component;

        private String redirect;

        private String icon;

        private boolean hidden;

        private boolean alwaysShow;

        private boolean keepAlive;

        private List<String> roles = new ArrayList<>();

        private List<String> permissions = new ArrayList<>();

        private List<Route> children = new ArrayList<>();
    }
}
