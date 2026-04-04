package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Current user role and permission snapshot")
public record AuthPermissionSnapshotResponse(
    @Schema(description = "Current user ID", example = "1")
    Long userId,
    @Schema(description = "Current tenant ID", example = "tenant_001")
    String tenantId,
    @Schema(description = "Current user roles", example = "[\"ROLE_SUPER_ADMIN\"]")
    List<String> roles,
    @Schema(description = "Current user permissions", example = "[\"system:user:query\",\"system:user:create\"]")
    List<String> permissions
) {
}
