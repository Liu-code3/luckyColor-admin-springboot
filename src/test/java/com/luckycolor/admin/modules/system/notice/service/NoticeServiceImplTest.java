package com.luckycolor.admin.modules.system.notice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.notice.dataobject.NoticeDO;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import com.luckycolor.admin.modules.system.notice.service.impl.NoticeServiceImpl;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePageQuery;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePublishRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticeSaveRequest;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class NoticeServiceImplTest {

    @Test
    void shouldReturnNoticePage() {
        NoticeMapper mapper = Mockito.mock(NoticeMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(notice()), 1L));
        NoticeService service = new NoticeServiceImpl(mapper, noScopeBuilder());

        PageResult<NoticePageResponse> result = service.pageNotices(new NoticePageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(NoticePageResponse::noticeTitle).containsExactly("Platform Notice");
    }

    @Test
    void shouldReturnNoticeDetail() {
        NoticeMapper mapper = Mockito.mock(NoticeMapper.class);
        when(mapper.selectById(1L)).thenReturn(notice());
        NoticeService service = new NoticeServiceImpl(mapper, noScopeBuilder());

        NoticeDetailResponse result = service.getNotice(1L);

        assertThat(result.noticeContent()).isEqualTo("system maintenance");
    }

    @Test
    void shouldCreateDraftNotice() {
        NoticeMapper mapper = Mockito.mock(NoticeMapper.class);
        NoticeService service = new NoticeServiceImpl(mapper, noScopeBuilder());

        Long result = service.createNotice(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(NoticeDO.class));
    }

    @Test
    void shouldPublishNotice() {
        NoticeMapper mapper = Mockito.mock(NoticeMapper.class);
        NoticeDO notice = notice();
        notice.setPublishStatus(0);
        notice.setPublishTime(null);
        when(mapper.selectById(1L)).thenReturn(notice);
        NoticeService service = new NoticeServiceImpl(mapper, noScopeBuilder());
        NoticePublishRequest request = new NoticePublishRequest();
        request.setPublishStatus(1);

        service.publishNotice(1L, request);

        assertThat(notice.getPublishStatus()).isEqualTo(1);
        assertThat(notice.getPublishTime()).isNotNull();
        verify(mapper).updateById(notice);
    }

    @Test
    void shouldThrowWhenNoticeNotFound() {
        NoticeMapper mapper = Mockito.mock(NoticeMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        NoticeService service = new NoticeServiceImpl(mapper, noScopeBuilder());

        assertThatThrownBy(() -> service.getNotice(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private NoticeDO notice() {
        NoticeDO notice = new NoticeDO();
        notice.setId(1L);
        notice.setTenantId(1L);
        notice.setNoticeTitle("Platform Notice");
        notice.setNoticeType("SYSTEM");
        notice.setNoticeContent("system maintenance");
        notice.setPublishStatus(1);
        notice.setPublishTime(LocalDateTime.of(2026, 4, 1, 10, 0));
        notice.setSort(1);
        notice.setRemark("default");
        return notice;
    }

    private NoticeSaveRequest saveRequest() {
        NoticeSaveRequest request = new NoticeSaveRequest();
        request.setNoticeTitle("Platform Notice");
        request.setNoticeType("SYSTEM");
        request.setNoticeContent("system maintenance");
        request.setSort(1);
        request.setRemark("default");
        return request;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }
}
