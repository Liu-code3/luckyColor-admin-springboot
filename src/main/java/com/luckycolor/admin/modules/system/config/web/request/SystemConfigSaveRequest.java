package com.luckycolor.admin.modules.system.config.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemConfigSaveRequest {

    @NotBlank
    private String configKey;

    @NotBlank
    private String configName;

    @NotBlank
    private String configValue;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer sensitive;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    @NotNull
    private Integer sort;

    private String remark;
}
