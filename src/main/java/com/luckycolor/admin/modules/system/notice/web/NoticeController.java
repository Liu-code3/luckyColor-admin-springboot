package com.luckycolor.admin.modules.system.notice.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import com.luckycolor.admin.modules.system.notice.service.NoticeService;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePageQuery;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePublishRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticeSaveRequest;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notices")
@ConditionalOnBean(NoticeMapper.class)
@Validated
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/page")
    @RequirePermission("system:notice:query")
    public ApiResponse<PageResult<NoticePageResponse>> page(NoticePageQuery query) {
        return ApiResponse.success(noticeService.pageNotices(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:notice:query")
    public ApiResponse<NoticeDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getNotice(id));
    }

    @PostMapping
    @RequirePermission("system:notice:create")
    public ApiResponse<Long> create(@Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.success(noticeService.createNotice(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:notice:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        noticeService.updateNotice(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/publish")
    @RequirePermission("system:notice:publish")
    public ApiResponse<Boolean> publish(@PathVariable Long id, @Valid @RequestBody NoticePublishRequest request) {
        noticeService.publishNotice(id, request);
        return ApiResponse.success(true);
    }
}
