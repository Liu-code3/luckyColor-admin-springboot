package com.luckycolor.admin.modules.system.notice.service.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeWriteRequest {

    private String noticeTitle;

    private String noticeType;

    private String noticeContent;

    private Integer publishStatus;

    private LocalDateTime publishTime;

    private Integer sort;

    private String remark;
}
