package com.luckycolor.admin.modules.system.config.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigPageQuery;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigSaveRequest;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigDetailResponse;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;

public interface SystemConfigService {

    PageResult<SystemConfigPageResponse> pageConfigs(SystemConfigPageQuery query);

    SystemConfigDetailResponse getConfig(Long id);

    Long createConfig(SystemConfigSaveRequest request);

    void updateConfig(Long id, SystemConfigSaveRequest request);
}
