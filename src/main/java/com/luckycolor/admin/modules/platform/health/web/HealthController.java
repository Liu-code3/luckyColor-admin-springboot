package com.luckycolor.admin.modules.platform.health.web;

import com.luckycolor.admin.common.api.ApiResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Platform Info", description = "Platform health and version APIs")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final String applicationName;
    private final DataSource dataSource;

    public HealthController(
        @Value("${spring.application.name:luckycolor-admin-springboot}") String applicationName,
        @Nullable DataSource dataSource
    ) {
        this.applicationName = applicationName;
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    @Operation(summary = "Get application health status")
    public ApiResponse<HealthStatusResponse> health() {
        String database = resolveDatabaseStatus();
        String status = "down".equals(database) ? "error" : "ok";
        return ApiResponse.success(new HealthStatusResponse(status, Instant.now().toString(), database, applicationName));
    }

    private String resolveDatabaseStatus() {
        if (dataSource == null) {
            return "unknown";
        }
        try (Connection ignored = dataSource.getConnection()) {
            return "up";
        } catch (SQLException exception) {
            log.warn("failed to check datasource health", exception);
            return "down";
        }
    }
}
