package com.luckycolor.admin.infrastructure.tenant.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

class TenancyPropertiesBindingTest {

    @Test
    void shouldIgnoreLegacyAnonymousAuthPathsFromApplicationYaml() throws IOException {
        Properties properties = loadApplicationProperties();
        String yaml = properties.toString();

        assertThat(yaml).contains("/api/auth/captcha");
        assertThat(yaml).contains("/api/auth/captcha/challenge");
        assertThat(yaml).contains("/api/auth/captcha/verify");
        assertThat(yaml).contains("/api/auth/login");
        assertThat(yaml).contains("/api/file");
        assertThat(new TenancyProperties().getIgnoreTables()).contains("sys_menu");
    }

    private Properties loadApplicationProperties() throws IOException {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new FileSystemResource("src/main/resources/application.yml"));
        Properties properties = factoryBean.getObject();
        if (properties == null) {
            throw new IOException("Failed to load application.yml");
        }
        return properties;
    }
}
