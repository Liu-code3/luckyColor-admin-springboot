package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Current user profile")
public record AuthProfileResponse(
    @Schema(description = "Current user ID", example = "1")
    Long userId,
    @Schema(description = "Current user ID", example = "1")
    Long id,
    @Schema(description = "Current username", example = "admin")
    String username,
    @Schema(description = "Current nickname", example = "System Admin")
    String nickname,
    @Schema(description = "Current tenant ID", example = "tenant_001")
    String tenantId,
    @Schema(description = "Current tenant name", example = "Default Tenant")
    String tenantName,
    @Schema(description = "Current user roles", example = "[\"ROLE_SUPER_ADMIN\"]")
    List<String> roles,
    @Schema(description = "Current user roles", example = "[\"ROLE_SUPER_ADMIN\"]")
    List<String> roleCodes,
    @Schema(description = "Current menu codes", example = "[\"main_system\",\"main_system_users\"]")
    List<String> menuCodeList,
    @Schema(description = "Current user permission codes", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> buttonCodeList,
    @Schema(description = "Current user permission codes", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> buttonCodes,
    @Schema(description = "Current user permission codes", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> permissions,
    @Schema(description = "Current user permission codes", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> permissionCodes,
    @Schema(description = "Current data scope type", example = "TENANT")
    String dataScopeType,
    @Schema(description = "Current data scope department IDs", example = "[100,101]")
    List<Long> dataScopeDeptIds
) {
}
