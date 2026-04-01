package com.luckycolor.admin.modules.system.notice.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticePageQuery extends PageQuery {

    private String noticeTitle;

    private String noticeType;

    private Integer publishStatus;
}
