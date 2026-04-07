package com.luckycolor.admin.modules.system.notice.service.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticePublishCommand {

    private Integer publishStatus;

    private LocalDateTime publishTime;

    private String remark;
}
