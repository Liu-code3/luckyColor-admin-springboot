package com.luckycolor.admin.modules.platform.health.web;

public record VersionInfoResponse(
    String applicationName,
    String version,
    String releaseStage,
    String docsPath
) {
}
