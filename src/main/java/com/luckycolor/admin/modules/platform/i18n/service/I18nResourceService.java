package com.luckycolor.admin.modules.platform.i18n.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourcePageQuery;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceSaveRequest;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceStatusRequest;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourceDetailResponse;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourcePageResponse;

public interface I18nResourceService {

    PageResult<I18nResourcePageResponse> pageResources(I18nResourcePageQuery query);

    I18nResourceDetailResponse getResource(Long id);

    Long createResource(I18nResourceSaveRequest request);

    void updateResource(Long id, I18nResourceSaveRequest request);

    void updateStatus(Long id, I18nResourceStatusRequest request);

    void bumpVersion(Long id);
}
