package com.luckycolor.admin.modules.system.notice.web.response;

import java.time.LocalDateTime;

public record NoticePageResponse(
    Long id,
    Long tenantId,
    String noticeTitle,
    String noticeType,
    Integer publishStatus,
    LocalDateTime publishTime,
    Integer sort,
    String remark
) {
}
