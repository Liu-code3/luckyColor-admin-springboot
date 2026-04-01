package com.luckycolor.admin.modules.system.role.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemRoleSaveRequest {

    @NotBlank
    private String roleCode;

    @NotBlank
    private String roleName;

    @NotNull
    private Integer sort;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    private String remark;
}
