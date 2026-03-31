package com.luckycolor.admin.modules.tenant.packageinfo.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantPackageSaveRequest {

    @NotBlank
    @Size(max = 100)
    private String packageName;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    @NotNull
    @Min(0)
    @Max(9999)
    private Integer sort;

    @Size(max = 500)
    private String remark;
}
