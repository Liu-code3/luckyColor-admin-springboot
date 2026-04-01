package com.luckycolor.admin.modules.platform.watermark.service;

import com.luckycolor.admin.modules.platform.watermark.web.request.WatermarkConfigSaveRequest;
import com.luckycolor.admin.modules.platform.watermark.web.response.WatermarkConfigResponse;

public interface WatermarkConfigService {

    WatermarkConfigResponse getCurrentConfig(Long tenantId);

    void saveCurrentConfig(Long tenantId, WatermarkConfigSaveRequest request);
}
