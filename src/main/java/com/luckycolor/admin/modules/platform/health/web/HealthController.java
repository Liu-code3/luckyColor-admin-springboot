package com.luckycolor.admin.modules.platform.health.web;

import com.luckycolor.admin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name:luckycolor-admin-springboot}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/health")
    public ApiResponse<HealthStatusResponse> health() {
        return ApiResponse.success(new HealthStatusResponse("UP", applicationName));
    }
}
