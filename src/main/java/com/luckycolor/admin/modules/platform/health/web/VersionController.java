package com.luckycolor.admin.modules.platform.health.web;

import com.luckycolor.admin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    private final String applicationName;
    private final String releaseStage;
    private final String docsPath;
    private final BuildProperties buildProperties;

    public VersionController(
        @Value("${spring.application.name:luckycolor-admin-springboot}") String applicationName,
        @Value("${info.app.stage:alpha}") String releaseStage,
        @Value("${info.app.docs-path:/api/docs}") String docsPath,
        @Nullable BuildProperties buildProperties
    ) {
        this.applicationName = applicationName;
        this.releaseStage = releaseStage;
        this.docsPath = docsPath;
        this.buildProperties = buildProperties;
    }

    @GetMapping("/version")
    public ApiResponse<VersionInfoResponse> version() {
        return ApiResponse.success(new VersionInfoResponse(
            applicationName,
            buildProperties == null ? "unknown" : buildProperties.getVersion(),
            releaseStage,
            docsPath
        ));
    }
}
