package com.luckycolor.admin.modules.platform.docs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiDocsWebMvcConfigurer implements WebMvcConfigurer {

    private static final String SWAGGER_UI_WEBJAR_VERSION = "5.32.0";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/assets/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/" + SWAGGER_UI_WEBJAR_VERSION + "/");
    }
}
