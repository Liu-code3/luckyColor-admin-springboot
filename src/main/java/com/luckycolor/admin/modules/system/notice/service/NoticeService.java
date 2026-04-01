package com.luckycolor.admin.modules.system.notice.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePageQuery;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePublishRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticeSaveRequest;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;

public interface NoticeService {

    PageResult<NoticePageResponse> pageNotices(NoticePageQuery query);

    NoticeDetailResponse getNotice(Long id);

    Long createNotice(NoticeSaveRequest request);

    void updateNotice(Long id, NoticeSaveRequest request);

    void publishNotice(Long id, NoticePublishRequest request);
}
