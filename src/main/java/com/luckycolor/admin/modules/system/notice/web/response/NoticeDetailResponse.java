package com.luckycolor.admin.modules.system.notice.web.response;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
    Long id,
    Long tenantId,
    String noticeTitle,
    String noticeType,
    String noticeContent,
    Integer publishStatus,
    LocalDateTime publishTime,
    Integer sort,
    String remark
) {
}
