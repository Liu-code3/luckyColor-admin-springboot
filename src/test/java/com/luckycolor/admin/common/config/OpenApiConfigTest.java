package com.luckycolor.admin.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

class OpenApiConfigTest {

    @Test
    void shouldUseBuildVersionWhenBuildInfoIsAvailable() {
        BuildProperties buildProperties = mock(BuildProperties.class);
        when(buildProperties.getVersion()).thenReturn("1.0.0-alpha.1");

        OpenAPI openAPI = new OpenApiConfig(buildProperties).luckyColorOpenApi();

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0-alpha.1");
    }

    @Test
    void shouldFallbackToUnknownVersionWhenBuildInfoIsMissing() {
        OpenAPI openAPI = new OpenApiConfig(null).luckyColorOpenApi();

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("unknown");
    }
}
