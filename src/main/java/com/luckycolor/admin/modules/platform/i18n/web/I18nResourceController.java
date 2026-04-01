package com.luckycolor.admin.modules.platform.i18n.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.platform.i18n.mapper.I18nResourceMapper;
import com.luckycolor.admin.modules.platform.i18n.service.I18nResourceService;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourcePageQuery;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceSaveRequest;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceStatusRequest;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourceDetailResponse;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourcePageResponse;
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
@RequestMapping("/admin/i18n-resources")
@Validated
@ConditionalOnBean(I18nResourceMapper.class)
public class I18nResourceController {

    private final I18nResourceService i18nResourceService;

    public I18nResourceController(I18nResourceService i18nResourceService) {
        this.i18nResourceService = i18nResourceService;
    }

    @GetMapping("/page")
    @RequirePermission("i18n:query")
    public ApiResponse<PageResult<I18nResourcePageResponse>> page(I18nResourcePageQuery query) {
        return ApiResponse.success(i18nResourceService.pageResources(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("i18n:query")
    public ApiResponse<I18nResourceDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(i18nResourceService.getResource(id));
    }

    @PostMapping
    @RequirePermission("i18n:create")
    public ApiResponse<Long> create(@Valid @RequestBody I18nResourceSaveRequest request) {
        return ApiResponse.success(i18nResourceService.createResource(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("i18n:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody I18nResourceSaveRequest request) {
        i18nResourceService.updateResource(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("i18n:update")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody I18nResourceStatusRequest request) {
        i18nResourceService.updateStatus(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/version")
    @RequirePermission("i18n:update")
    public ApiResponse<Boolean> bumpVersion(@PathVariable Long id) {
        i18nResourceService.bumpVersion(id);
        return ApiResponse.success(true);
    }
}
