package com.luckycolor.admin.modules.platform.codegen.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodegenColumnSaveItem {

    @NotNull
    private Long id;

    @NotBlank
    private String javaType;

    @NotBlank
    private String javaField;

    @NotBlank
    private String htmlType;

    @NotBlank
    private String queryType;

    @NotNull
    private Integer required;

    @NotNull
    private Integer listVisible;

    @NotNull
    private Integer formVisible;

    @NotNull
    private Integer status;
}
