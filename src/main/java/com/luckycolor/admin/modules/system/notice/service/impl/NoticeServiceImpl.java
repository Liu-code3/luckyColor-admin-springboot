package com.luckycolor.admin.modules.system.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.notice.dataobject.NoticeDO;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import com.luckycolor.admin.modules.system.notice.service.NoticeService;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePageQuery;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePublishRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticeSaveRequest;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;
import java.time.LocalDateTime;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnPersistenceEnabled
public class NoticeServiceImpl implements NoticeService {

    private static final int DRAFT = 0;

    private static final int PUBLISHED = 1;

    private final NoticeMapper noticeMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public NoticeServiceImpl(NoticeMapper noticeMapper, DataScopeConditionBuilder dataScopeConditionBuilder) {
        this.noticeMapper = noticeMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<NoticePageResponse> pageNotices(NoticePageQuery query) {
        PageResult<NoticeDO> pageResult = noticeMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public NoticeDetailResponse getNotice(Long id) {
        return toDetailResponse(getRequiredNotice(id));
    }

    @Override
    public Long createNotice(NoticeSaveRequest request) {
        NoticeDO notice = new NoticeDO();
        fillNotice(notice, request);
        notice.setPublishStatus(DRAFT);
        noticeMapper.insert(notice);
        return notice.getId();
    }

    @Override
    public void updateNotice(Long id, NoticeSaveRequest request) {
        NoticeDO notice = getRequiredNotice(id);
        fillNotice(notice, request);
        noticeMapper.updateById(notice);
    }

    @Override
    public void publishNotice(Long id, NoticePublishRequest request) {
        NoticeDO notice = getRequiredNotice(id);
        notice.setPublishStatus(request.getPublishStatus());
        notice.setPublishTime(request.getPublishStatus() == PUBLISHED ? LocalDateTime.now() : null);
        noticeMapper.updateById(notice);
    }

    private LambdaQueryWrapper<NoticeDO> buildQueryWrapper(NoticePageQuery query) {
        LambdaQueryWrapper<NoticeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getNoticeTitle()), NoticeDO::getNoticeTitle, query.getNoticeTitle());
        queryWrapper.eq(StringUtils.hasText(query.getNoticeType()), NoticeDO::getNoticeType, query.getNoticeType());
        queryWrapper.eq(query.getPublishStatus() != null, NoticeDO::getPublishStatus, query.getPublishStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, NoticeDO::getTenantId, null);
        queryWrapper.orderByDesc(NoticeDO::getPublishTime)
            .orderByAsc(NoticeDO::getSort)
            .orderByDesc(NoticeDO::getCreateTime);
        return queryWrapper;
    }

    private NoticeDO getRequiredNotice(Long id) {
        NoticeDO notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found");
        }
        return notice;
    }

    private void fillNotice(NoticeDO notice, NoticeSaveRequest request) {
        notice.setNoticeTitle(request.getNoticeTitle());
        notice.setNoticeType(request.getNoticeType());
        notice.setNoticeContent(request.getNoticeContent());
        notice.setSort(request.getSort());
        notice.setRemark(request.getRemark());
    }

    private NoticePageResponse toPageResponse(NoticeDO notice) {
        return new NoticePageResponse(
            notice.getId(),
            notice.getTenantId(),
            notice.getNoticeTitle(),
            notice.getNoticeType(),
            notice.getPublishStatus(),
            notice.getPublishTime(),
            notice.getSort(),
            notice.getRemark()
        );
    }

    private NoticeDetailResponse toDetailResponse(NoticeDO notice) {
        return new NoticeDetailResponse(
            notice.getId(),
            notice.getTenantId(),
            notice.getNoticeTitle(),
            notice.getNoticeType(),
            notice.getNoticeContent(),
            notice.getPublishStatus(),
            notice.getPublishTime(),
            notice.getSort(),
            notice.getRemark()
        );
    }
}
