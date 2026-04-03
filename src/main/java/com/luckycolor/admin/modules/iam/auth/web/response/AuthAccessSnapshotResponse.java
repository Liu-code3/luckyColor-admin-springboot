package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Current user access snapshot")
public record AuthAccessSnapshotResponse(
    @Schema(description = "Current user snapshot")
    AuthAccessUserResponse user,
    @Schema(description = "Current user roles")
    List<AuthAccessRoleResponse> roles,
    @Schema(description = "Current user menu tree")
    List<AuthAccessMenuTreeItemResponse> menuTree
) {

    @Schema(description = "Current user snapshot")
    public record AuthAccessUserResponse(
        @Schema(description = "Current user ID", example = "1")
        Long id,
        @Schema(description = "Current tenant ID", example = "1")
        Long tenantId,
        @Schema(description = "Current username", example = "admin")
        String username,
        @Schema(description = "Current user nickname", example = "System Admin")
        String nickname,
        @Schema(description = "Current role codes", example = "[\"ROLE_SUPER_ADMIN\"]")
        List<String> roleCodes,
        @Schema(description = "Current menu permission codes", example = "[\"dashboard\",\"system:user\"]")
        List<String> menuCodeList,
        @Schema(description = "Current button permission codes", example = "[\"dashboard:query\",\"system:user:query\"]")
        List<String> buttonCodeList
    ) {
    }

    @Schema(description = "Current role snapshot")
    public record AuthAccessRoleResponse(
        @Schema(description = "Current tenant ID", example = "1")
        Long tenantId,
        @Schema(description = "Role ID", example = "1")
        String id,
        @Schema(description = "Role name", example = "ROLE_SUPER_ADMIN")
        String name,
        @Schema(description = "Role code", example = "ROLE_SUPER_ADMIN")
        String code
    ) {
    }

    @Schema(description = "Current menu tree item")
    public record AuthAccessMenuTreeItemResponse(
        @Schema(description = "Parent menu ID, root node is 0", example = "0")
        Long pid,
        @Schema(description = "Menu ID", example = "1")
        Long id,
        @Schema(description = "Menu title", example = "Dashboard")
        String title,
        @Schema(description = "Route name", example = "Dashboard")
        String name,
        @Schema(description = "Menu type", example = "2")
        Integer type,
        @Schema(description = "Route path", example = "/dashboard")
        String path,
        @Schema(description = "Menu route key", example = "dashboard")
        String key,
        @Schema(description = "Permission code", example = "dashboard")
        String permissionCode,
        @Schema(description = "Icon name", example = "dashboard")
        String icon,
        @Schema(description = "Layout identifier", example = "default")
        String layout,
        @Schema(description = "Whether visible", example = "true")
        Boolean isVisible,
        @Schema(description = "Whether enabled", example = "true")
        Boolean status,
        @Schema(description = "Frontend component path", example = "dashboard/index")
        String component,
        @Schema(description = "Redirect path", example = "/dashboard/analysis")
        String redirect,
        @Schema(description = "Route meta object")
        Map<String, Object> meta,
        @Schema(description = "Sort order", example = "1")
        Integer sort,
        @Schema(description = "Created time", example = "2026-03-22T14:30:00.000Z")
        String createdAt,
        @Schema(description = "Updated time", example = "2026-03-22T15:00:00.000Z")
        String updatedAt,
        @Schema(description = "Child menu nodes")
        List<AuthAccessMenuTreeItemResponse> children
    ) {
    }
}
