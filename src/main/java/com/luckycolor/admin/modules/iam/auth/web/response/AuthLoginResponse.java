package com.luckycolor.admin.modules.iam.auth.web.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Login result payload")
public record AuthLoginResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.demo.signature")
    String accessToken,
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,
    @Schema(description = "Token expiration in seconds", example = "7200")
    long expiresIn,
    @JsonIgnore
    String refreshToken,
    @Schema(description = "Current user ID", example = "1")
    Long userId,
    @Schema(description = "Current username", example = "admin")
    String username,
    @Schema(description = "Current user nickname", example = "System Admin")
    String nickname,
    @Schema(description = "Current tenant ID", example = "tenant_001")
    String tenantId,
    @Schema(description = "Current user roles", example = "[\"ROLE_SUPER_ADMIN\"]")
    List<String> roles,
    @Schema(description = "Permission code list", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> buttonCodeList,
    @Schema(description = "Permission code list", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> buttonCodes,
    @Schema(description = "Permission code list", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> permissions,
    @Schema(description = "Permission code list", example = "[\"dashboard:query\",\"system:user:query\"]")
    List<String> permissionCodes,
    @Schema(description = "Current menu codes", example = "[\"main_system\",\"main_system_users\"]")
    List<String> menuCodeList,
    @Schema(description = "Current data scope type", example = "TENANT")
    String dataScopeType,
    @Schema(description = "Current data scope department IDs", example = "[100,101]")
    List<Long> dataScopeDeptIds,
    @Schema(description = "Nested current user payload")
    AuthLoginUserResponse user
) {
}
