package com.luckycolor.admin.infrastructure.tenant.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.tenancy")
public class TenancyProperties {

    private boolean enabled = true;

    private String header = "x-tenant-id";

    private String domainSuffix;

    private String defaultTenantId;

    private List<String> ignorePaths = new ArrayList<>(List.of(
        "/auth/captcha",
        "/api/auth/captcha",
        "/auth/captcha/challenge",
        "/api/auth/captcha/challenge",
        "/auth/captcha/verify",
        "/api/auth/captcha/verify",
        "/auth/login",
        "/api/auth/login",
        "/health",
        "/api/health",
        "/version",
        "/api/version",
        "/docs",
        "/api/docs",
        "/swagger-ui",
        "/api/swagger-ui",
        "/v3/api-docs",
        "/api/v3/api-docs",
        "/actuator",
        "/api/actuator"
    ));

    private List<String> ignoreTables = new ArrayList<>(List.of(
        "sys_tenant",
        "sys_tenant_package",
        "sys_tenant_audit_log",
        "sys_tenant_bootstrap_record",
        "sys_security_audit_log",
        "sys_menu"
    ));

    public boolean isIgnoredPath(String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return ignorePaths.stream().anyMatch(path::startsWith);
    }

    public boolean isIgnoredTable(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return false;
        }
        return ignoreTables.stream()
            .map(name -> name.toLowerCase(Locale.ROOT))
            .anyMatch(tableName.toLowerCase(Locale.ROOT)::equals);
    }
}
