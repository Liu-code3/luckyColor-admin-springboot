package com.luckycolor.admin.modules.system.user.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "System user list item")
public record SystemUserPageResponse(
    @Schema(description = "User ID", example = "1001")
    Long id,
    @Schema(description = "Tenant ID", example = "1")
    Long tenantId,
    @Schema(description = "Username", example = "admin")
    String username,
    @Schema(description = "Nickname", example = "System Admin")
    String nickname,
    @Schema(description = "Email", example = "admin@example.com")
    String email,
    @Schema(description = "Mobile number", example = "13800138000")
    String mobile,
    @Schema(description = "Department ID", example = "100")
    Long departmentId,
    @Schema(description = "Role codes", example = "[\"super_admin\"]")
    List<String> roleCodes,
    @Schema(description = "Status: 0 enabled, 1 disabled", example = "0")
    Integer status
) {
}
