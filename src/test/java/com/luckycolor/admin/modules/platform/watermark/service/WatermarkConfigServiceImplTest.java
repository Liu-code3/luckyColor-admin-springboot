package com.luckycolor.admin.modules.platform.watermark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.modules.platform.watermark.dataobject.WatermarkConfigDO;
import com.luckycolor.admin.modules.platform.watermark.mapper.WatermarkConfigMapper;
import com.luckycolor.admin.modules.platform.watermark.service.impl.WatermarkConfigServiceImpl;
import com.luckycolor.admin.modules.platform.watermark.web.request.WatermarkConfigSaveRequest;
import com.luckycolor.admin.modules.platform.watermark.web.response.WatermarkConfigResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class WatermarkConfigServiceImplTest {

    @Test
    void shouldReturnDefaultConfigWhenMissing() {
        WatermarkConfigMapper mapper = Mockito.mock(WatermarkConfigMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        WatermarkConfigService service = new WatermarkConfigServiceImpl(mapper);

        WatermarkConfigResponse response = service.getCurrentConfig(1L);

        assertThat(response.tenantId()).isEqualTo(1L);
        assertThat(response.enabled()).isEqualTo(0);
        assertThat(response.content()).isEqualTo("LuckyColor");
        assertThat(response.opacityPercent()).isEqualTo(15);
    }

    @Test
    void shouldReturnStoredConfig() {
        WatermarkConfigMapper mapper = Mockito.mock(WatermarkConfigMapper.class);
        when(mapper.selectOne(any())).thenReturn(config());
        WatermarkConfigService service = new WatermarkConfigServiceImpl(mapper);

        WatermarkConfigResponse response = service.getCurrentConfig(1L);

        assertThat(response.enabled()).isEqualTo(1);
        assertThat(response.content()).isEqualTo("LuckyColor Tenant");
        assertThat(response.gapY()).isEqualTo(160);
    }

    @Test
    void shouldCreateConfigWhenMissing() {
        WatermarkConfigMapper mapper = Mockito.mock(WatermarkConfigMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        WatermarkConfigService service = new WatermarkConfigServiceImpl(mapper);

        service.saveCurrentConfig(1L, saveRequest());

        ArgumentCaptor<WatermarkConfigDO> captor = ArgumentCaptor.forClass(WatermarkConfigDO.class);
        verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(1L);
        assertThat(captor.getValue().getContent()).isEqualTo("LuckyColor Tenant");
    }

    @Test
    void shouldUpdateConfigWhenExists() {
        WatermarkConfigMapper mapper = Mockito.mock(WatermarkConfigMapper.class);
        WatermarkConfigDO existing = config();
        when(mapper.selectOne(any())).thenReturn(existing);
        WatermarkConfigService service = new WatermarkConfigServiceImpl(mapper);

        service.saveCurrentConfig(1L, saveRequest());

        verify(mapper).updateById(existing);
        assertThat(existing.getEnabled()).isEqualTo(1);
        assertThat(existing.getRotateDegree()).isEqualTo(-30);
    }

    private WatermarkConfigDO config() {
        WatermarkConfigDO config = new WatermarkConfigDO();
        config.setId(1L);
        config.setTenantId(1L);
        config.setEnabled(1);
        config.setContent("LuckyColor Tenant");
        config.setColor("#1677ff");
        config.setFontSize(18);
        config.setOpacityPercent(20);
        config.setRotateDegree(-30);
        config.setGapX(140);
        config.setGapY(160);
        return config;
    }

    private WatermarkConfigSaveRequest saveRequest() {
        WatermarkConfigSaveRequest request = new WatermarkConfigSaveRequest();
        request.setEnabled(1);
        request.setContent("LuckyColor Tenant");
        request.setColor("#1677ff");
        request.setFontSize(18);
        request.setOpacityPercent(20);
        request.setRotateDegree(-30);
        request.setGapX(140);
        request.setGapY(160);
        return request;
    }
}
