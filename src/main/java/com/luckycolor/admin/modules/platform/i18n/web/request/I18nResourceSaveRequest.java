package com.luckycolor.admin.modules.platform.i18n.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class I18nResourceSaveRequest {

    @NotBlank
    private String locale;

    @NotBlank
    private String namespace;

    @NotBlank
    private String resourceKey;

    @NotBlank
    private String resourceValue;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    private String remark;
}
