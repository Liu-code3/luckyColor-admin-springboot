package com.luckycolor.admin.modules.system.notice.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.notice.service.NoticeService;
import com.luckycolor.admin.modules.system.notice.service.request.NoticePublishCommand;
import com.luckycolor.admin.modules.system.notice.service.request.NoticeWriteRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePageQuery;
import com.luckycolor.admin.modules.system.notice.web.request.NoticePublishRequest;
import com.luckycolor.admin.modules.system.notice.web.request.NoticeSaveRequest;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
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
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "System Notices", description = "Notice management APIs")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/page")
    @RequirePermission("system:notice:query")
    @Operation(summary = "Page notices")
    public ApiResponse<PageResult<NoticePageResponse>> page(@ParameterObject NoticePageQuery query) {
        return ApiResponse.success(noticeService.pageNotices(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:notice:query")
    @Operation(summary = "Get notice detail")
    public ApiResponse<NoticeDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getNotice(id));
    }

    @PostMapping
    @RequirePermission("system:notice:create")
    @Operation(summary = "Create notice")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notice created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"title must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Long> create(@Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.success(noticeService.createNotice(toWriteRequest(request)));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:notice:update")
    @Operation(summary = "Update notice")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        noticeService.updateNotice(id, toWriteRequest(request));
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/publish")
    @RequirePermission("system:notice:publish")
    @Operation(summary = "Publish notice")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notice published successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Boolean> publish(@PathVariable Long id, @Valid @RequestBody NoticePublishRequest request) {
        noticeService.publishNotice(id, toPublishCommand(request));
        return ApiResponse.success(true);
    }

    private NoticeWriteRequest toWriteRequest(NoticeSaveRequest request) {
        NoticeWriteRequest target = new NoticeWriteRequest();
        target.setNoticeTitle(request.getNoticeTitle());
        target.setNoticeType(request.getNoticeType());
        target.setNoticeContent(request.getNoticeContent());
        target.setSort(request.getSort());
        target.setRemark(request.getRemark());
        return target;
    }

    private NoticePublishCommand toPublishCommand(NoticePublishRequest request) {
        NoticePublishCommand target = new NoticePublishCommand();
        target.setPublishStatus(request.getPublishStatus());
        return target;
    }
}
