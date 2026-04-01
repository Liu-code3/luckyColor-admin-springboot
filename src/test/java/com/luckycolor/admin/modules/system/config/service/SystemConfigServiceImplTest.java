package com.luckycolor.admin.modules.system.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.config.dataobject.SystemConfigDO;
import com.luckycolor.admin.modules.system.config.mapper.SystemConfigMapper;
import com.luckycolor.admin.modules.system.config.service.impl.SystemConfigServiceImpl;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigPageQuery;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigSaveRequest;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigDetailResponse;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class SystemConfigServiceImplTest {

    @Test
    void shouldReturnSystemConfigPage() {
        SystemConfigMapper mapper = Mockito.mock(SystemConfigMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(config()), 1L));
        SystemConfigService service = new SystemConfigServiceImpl(mapper, noScopeBuilder());

        PageResult<SystemConfigPageResponse> result = service.pageConfigs(new SystemConfigPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList().get(0).configValue()).isEqualTo("******");
    }

    @Test
    void shouldReturnSystemConfigDetail() {
        SystemConfigMapper mapper = Mockito.mock(SystemConfigMapper.class);
        when(mapper.selectById(1L)).thenReturn(config());
        SystemConfigService service = new SystemConfigServiceImpl(mapper, noScopeBuilder());

        SystemConfigDetailResponse result = service.getConfig(1L);

        assertThat(result.configKey()).isEqualTo("sms.secret");
        assertThat(result.configValue()).isEqualTo("******");
    }

    @Test
    void shouldCreateSystemConfig() {
        SystemConfigMapper mapper = Mockito.mock(SystemConfigMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        SystemConfigService service = new SystemConfigServiceImpl(mapper, noScopeBuilder());

        Long result = service.createConfig(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(SystemConfigDO.class));
    }

    @Test
    void shouldThrowWhenSystemConfigNotFound() {
        SystemConfigMapper mapper = Mockito.mock(SystemConfigMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        SystemConfigService service = new SystemConfigServiceImpl(mapper, noScopeBuilder());

        assertThatThrownBy(() -> service.getConfig(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private SystemConfigDO config() {
        SystemConfigDO systemConfig = new SystemConfigDO();
        systemConfig.setId(1L);
        systemConfig.setTenantId(1L);
        systemConfig.setConfigKey("sms.secret");
        systemConfig.setConfigName("SMS Secret");
        systemConfig.setConfigValue("raw-secret");
        systemConfig.setSensitive(1);
        systemConfig.setStatus(0);
        systemConfig.setSort(1);
        systemConfig.setRemark("default");
        return systemConfig;
    }

    private SystemConfigSaveRequest saveRequest() {
        SystemConfigSaveRequest request = new SystemConfigSaveRequest();
        request.setConfigKey("sms.secret");
        request.setConfigName("SMS Secret");
        request.setConfigValue("raw-secret");
        request.setSensitive(1);
        request.setStatus(0);
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
