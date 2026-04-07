package com.luckycolor.admin.regression;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class TenantBootstrapPermissionMigrationTest {

    @Test
    void shouldExpandPermissionColumnsBeforeGrantingBootstrapPermissions() throws IOException {
        ClassPathResource resource = new ClassPathResource("db/migration/V26__grant_tenant_bootstrap_permissions.sql");

        assertThat(resource.exists()).isTrue();

        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(sql).contains("ALTER TABLE sys_role");
        assertThat(sql).contains("ALTER TABLE sys_user");
        assertThat(sql).contains("MODIFY COLUMN permission_codes VARCHAR(2000) NULL");
        assertThat(sql).contains("tenant:bootstrap:query");
        assertThat(sql).contains("tenant:bootstrap:execute");
    }
}
