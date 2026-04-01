package com.luckycolor.admin.modules.platform.codegen.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodegenTableSaveRequest {

    @NotBlank
    private String businessName;

    @NotBlank
    private String className;

    @NotBlank
    private String moduleName;

    @NotBlank
    private String packageName;

    @NotBlank
    private String genMode;

    private String remark;
}
