package com.luckycolor.admin.modules.system.department.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemDepartmentSaveRequest {

    @NotNull
    private Long parentId;

    @NotBlank
    private String departmentName;

    private String leader;

    private String phone;

    @Email
    private String email;

    @NotNull
    private Integer sort;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    private String remark;
}
