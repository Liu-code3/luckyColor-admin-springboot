package com.luckycolor.admin.modules.system.dictionary.item.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictionaryItemSaveRequest {

    @NotBlank
    private String typeCode;

    @NotNull
    private Long parentId;

    @NotBlank
    private String itemLabel;

    @NotBlank
    private String itemValue;

    private String itemTag;

    @NotNull
    private Integer sort;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    private String remark;
}
