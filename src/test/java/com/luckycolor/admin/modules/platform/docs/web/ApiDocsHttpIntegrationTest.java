package com.luckycolor.admin.modules.platform.docs.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "server.servlet.context-path=/api"
)
class ApiDocsHttpIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldServeDocsPageOverHttpWithoutAuthentication() {
        ResponseEntity<String> response = testRestTemplate.getForEntity(
            "http://localhost:" + port + "/api/docs",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("SwaggerUIBundle");
        assertThat(response.getBody()).contains("/api/v3/api-docs");
        assertThat(response.getBody()).contains("LuckyColor 接口文档");
        assertThat(response.getBody()).contains("认证授权");
    }

    @Test
    void shouldServeDocsAssetsOverHttpWithoutAuthentication() {
        ResponseEntity<String> response = testRestTemplate.getForEntity(
            "http://localhost:" + port + "/api/docs/assets/swagger-ui.css",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(".swagger-ui");
    }
}
