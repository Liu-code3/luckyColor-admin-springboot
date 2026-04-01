package com.luckycolor.admin.modules.system.dictionary.type.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictionaryTypeSaveRequest {

    @NotBlank
    private String typeCode;

    @NotBlank
    private String typeName;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    @NotNull
    private Integer sort;

    private String remark;
}
