package com.luckycolor.admin.modules.system.notice.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeSaveRequest {

    @NotBlank
    private String noticeTitle;

    @NotBlank
    private String noticeType;

    @NotBlank
    private String noticeContent;

    @NotNull
    private Integer sort;

    private String remark;
}
