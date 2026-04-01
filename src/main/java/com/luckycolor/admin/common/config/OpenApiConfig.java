package com.luckycolor.admin.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.lang.Nullable;

@Configuration
public class OpenApiConfig {

    private final BuildProperties buildProperties;

    public OpenApiConfig(@Nullable BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    OpenAPI luckyColorOpenApi() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("LuckyColor Admin Spring Boot API")
                .description("Spring Boot rewrite for the LuckyColor admin backend")
                .version(resolveVersion()))
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .components(new Components().addSecuritySchemes(
                schemeName,
                new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    private String resolveVersion() {
        return buildProperties == null ? "unknown" : buildProperties.getVersion();
    }
}
