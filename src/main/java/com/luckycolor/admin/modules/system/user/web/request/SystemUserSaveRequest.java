package com.luckycolor.admin.modules.system.user.web.request;

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
public class SystemUserSaveRequest {

    @NotBlank
    private String username;

    private String password;

    @NotBlank
    private String nickname;

    @Email
    private String email;

    private String mobile;

    private List<String> roleCodes = new ArrayList<>();

    private List<String> permissionCodes = new ArrayList<>();

    @NotBlank
    private String dataScope;

    private Long departmentId;

    private List<Long> departmentIds = new ArrayList<>();

    private List<Long> scopeTenantIds = new ArrayList<>();

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    private String remark;
}
