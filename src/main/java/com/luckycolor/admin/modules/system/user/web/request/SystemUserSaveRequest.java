package com.luckycolor.admin.modules.system.user.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Create or update system user request")
public class SystemUserSaveRequest {

    @NotBlank
    @Schema(description = "Username", example = "alice")
    private String username;

    @Schema(description = "Password, required when creating", example = "ChangeMe123!")
    private String password;

    @NotBlank
    @Schema(description = "Nickname", example = "Alice")
    private String nickname;

    @Email
    @Schema(description = "Email address", example = "alice@example.com")
    private String email;

    @Schema(description = "Mobile number", example = "13800138000")
    private String mobile;

    @Schema(description = "Role codes", example = "[\"tenant_admin\"]")
    private List<String> roleCodes = new ArrayList<>();

    @Schema(description = "Permission codes", example = "[\"system:user:query\"]")
    private List<String> permissionCodes = new ArrayList<>();

    @NotBlank
    @Schema(description = "Data scope strategy", example = "TENANT")
    private String dataScope;

    @Schema(description = "Primary department ID", example = "100")
    private Long departmentId;

    @Schema(description = "Department scope IDs", example = "[100,101]")
    private List<Long> departmentIds = new ArrayList<>();

    @Schema(description = "Tenant scope IDs", example = "[1]")
    private List<Long> scopeTenantIds = new ArrayList<>();

    @NotNull
    @Min(0)
    @Max(1)
    @Schema(description = "Status: 0 enabled, 1 disabled", example = "0")
    private Integer status;

    @Schema(description = "Remark", example = "Created from admin console")
    private String remark;
}
