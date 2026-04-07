package com.luckycolor.admin.modules.system.notice.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.notice.service.request.NoticePublishCommand;
import com.luckycolor.admin.modules.system.notice.service.request.NoticeWriteRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePageQuery;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;

public interface NoticeService {

    PageResult<NoticePageResponse> pageNotices(NoticePageQuery query);

    NoticeDetailResponse getNotice(Long id);

    Long createNotice(NoticeWriteRequest request);

    void updateNotice(Long id, NoticeWriteRequest request);

    void publishNotice(Long id, NoticePublishCommand request);
}
