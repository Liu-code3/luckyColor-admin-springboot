package com.luckycolor.admin.modules.tenant.bootstrap.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.tenancy.bootstrap")
public class TenantBootstrapProperties {

    private List<Template> templates = new ArrayList<>();

    @Getter
    @Setter
    public static class Template {

        private String code;

        private String name;

        private List<String> roleCodes = new ArrayList<>();

        private List<String> menuCodes = new ArrayList<>();

        private String adminUsername = "admin";

        private String adminNickname = "Tenant Admin";

        private Integer status = 0;

        private String remark;
    }
}
