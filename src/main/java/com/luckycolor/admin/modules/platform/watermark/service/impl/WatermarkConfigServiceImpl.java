package com.luckycolor.admin.modules.platform.watermark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.modules.platform.watermark.dataobject.WatermarkConfigDO;
import com.luckycolor.admin.modules.platform.watermark.mapper.WatermarkConfigMapper;
import com.luckycolor.admin.modules.platform.watermark.service.WatermarkConfigService;
import com.luckycolor.admin.modules.platform.watermark.web.request.WatermarkConfigSaveRequest;
import com.luckycolor.admin.modules.platform.watermark.web.response.WatermarkConfigResponse;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnPersistenceEnabled
public class WatermarkConfigServiceImpl implements WatermarkConfigService {

    private static final int DEFAULT_ENABLED = 0;
    private static final String DEFAULT_CONTENT = "LuckyColor";
    private static final String DEFAULT_COLOR = "#000000";
    private static final int DEFAULT_FONT_SIZE = 16;
    private static final int DEFAULT_OPACITY_PERCENT = 15;
    private static final int DEFAULT_ROTATE_DEGREE = -22;
    private static final int DEFAULT_GAP_X = 120;
    private static final int DEFAULT_GAP_Y = 120;

    private final WatermarkConfigMapper watermarkConfigMapper;

    public WatermarkConfigServiceImpl(WatermarkConfigMapper watermarkConfigMapper) {
        this.watermarkConfigMapper = watermarkConfigMapper;
    }

    @Override
    public WatermarkConfigResponse getCurrentConfig(Long tenantId) {
        WatermarkConfigDO config = findByTenantId(tenantId);
        if (config == null) {
            return buildDefaultConfig(tenantId);
        }
        return toResponse(config);
    }

    @Override
    public void saveCurrentConfig(Long tenantId, WatermarkConfigSaveRequest request) {
        WatermarkConfigDO config = findByTenantId(tenantId);
        if (config == null) {
            config = new WatermarkConfigDO();
            config.setTenantId(tenantId);
            fillConfig(config, request);
            watermarkConfigMapper.insert(config);
            return;
        }
        fillConfig(config, request);
        watermarkConfigMapper.updateById(config);
    }

    private WatermarkConfigDO findByTenantId(Long tenantId) {
        LambdaQueryWrapper<WatermarkConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        if (tenantId == null) {
            queryWrapper.isNull(WatermarkConfigDO::getTenantId);
        } else {
            queryWrapper.eq(WatermarkConfigDO::getTenantId, tenantId);
        }
        return watermarkConfigMapper.selectOne(queryWrapper);
    }

    private void fillConfig(WatermarkConfigDO config, WatermarkConfigSaveRequest request) {
        config.setEnabled(request.getEnabled());
        config.setContent(trim(request.getContent()));
        config.setColor(trim(request.getColor()));
        config.setFontSize(request.getFontSize());
        config.setOpacityPercent(request.getOpacityPercent());
        config.setRotateDegree(request.getRotateDegree());
        config.setGapX(request.getGapX());
        config.setGapY(request.getGapY());
    }

    private WatermarkConfigResponse buildDefaultConfig(Long tenantId) {
        return new WatermarkConfigResponse(
            tenantId,
            DEFAULT_ENABLED,
            DEFAULT_CONTENT,
            DEFAULT_COLOR,
            DEFAULT_FONT_SIZE,
            DEFAULT_OPACITY_PERCENT,
            DEFAULT_ROTATE_DEGREE,
            DEFAULT_GAP_X,
            DEFAULT_GAP_Y
        );
    }

    private WatermarkConfigResponse toResponse(WatermarkConfigDO config) {
        return new WatermarkConfigResponse(
            config.getTenantId(),
            config.getEnabled(),
            config.getContent(),
            config.getColor(),
            config.getFontSize(),
            config.getOpacityPercent(),
            config.getRotateDegree(),
            config.getGapX(),
            config.getGapY()
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
