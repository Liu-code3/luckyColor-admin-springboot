package com.luckycolor.admin.regression;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SeedBootstrapMigrationTest {

    @Test
    void shouldContainCoreSeedStatements() throws IOException {
        ClassPathResource resource = new ClassPathResource("db/migration/V16__seed_bootstrap_data.sql");

        assertThat(resource.exists()).isTrue();

        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(sql).contains("INSERT IGNORE INTO sys_tenant_package");
        assertThat(sql).contains("INSERT IGNORE INTO sys_tenant");
        assertThat(sql).contains("INSERT IGNORE INTO sys_menu");
        assertThat(sql).contains("INSERT IGNORE INTO sys_role");
        assertThat(sql).contains("INSERT IGNORE INTO sys_user");
        assertThat(sql).contains("INSERT IGNORE INTO sys_dictionary_type");
        assertThat(sql).contains("INSERT IGNORE INTO sys_dictionary_item");
    }
}
