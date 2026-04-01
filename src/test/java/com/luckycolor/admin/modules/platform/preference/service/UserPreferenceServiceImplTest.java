package com.luckycolor.admin.modules.platform.preference.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.modules.platform.preference.dataobject.UserPreferenceDO;
import com.luckycolor.admin.modules.platform.preference.mapper.UserPreferenceMapper;
import com.luckycolor.admin.modules.platform.preference.service.impl.UserPreferenceServiceImpl;
import com.luckycolor.admin.modules.platform.preference.web.request.UserPreferenceSaveRequest;
import com.luckycolor.admin.modules.platform.preference.web.response.UserPreferenceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class UserPreferenceServiceImplTest {

    @Test
    void shouldReturnDefaultPreferenceWhenMissing() {
        UserPreferenceMapper mapper = Mockito.mock(UserPreferenceMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        UserPreferenceService service = new UserPreferenceServiceImpl(mapper);

        UserPreferenceResponse response = service.getCurrentPreference(1L, 1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.tenantId()).isEqualTo(1L);
        assertThat(response.themeScheme()).isEqualTo("light");
        assertThat(response.layoutMode()).isEqualTo("side");
        assertThat(response.locale()).isEqualTo("zh-CN");
    }

    @Test
    void shouldReturnStoredPreference() {
        UserPreferenceMapper mapper = Mockito.mock(UserPreferenceMapper.class);
        when(mapper.selectOne(any())).thenReturn(preference());
        UserPreferenceService service = new UserPreferenceServiceImpl(mapper);

        UserPreferenceResponse response = service.getCurrentPreference(1L, 1L);

        assertThat(response.themeScheme()).isEqualTo("dark");
        assertThat(response.themeColor()).isEqualTo("#13c2c2");
        assertThat(response.sidebarCollapsed()).isEqualTo(1);
    }

    @Test
    void shouldCreatePreferenceWhenMissing() {
        UserPreferenceMapper mapper = Mockito.mock(UserPreferenceMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        UserPreferenceService service = new UserPreferenceServiceImpl(mapper);

        service.saveCurrentPreference(1L, 1L, saveRequest());

        ArgumentCaptor<UserPreferenceDO> captor = ArgumentCaptor.forClass(UserPreferenceDO.class);
        verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getTenantId()).isEqualTo(1L);
        assertThat(captor.getValue().getThemeScheme()).isEqualTo("dark");
    }

    @Test
    void shouldUpdatePreferenceWhenExists() {
        UserPreferenceMapper mapper = Mockito.mock(UserPreferenceMapper.class);
        UserPreferenceDO existing = preference();
        when(mapper.selectOne(any())).thenReturn(existing);
        UserPreferenceService service = new UserPreferenceServiceImpl(mapper);

        service.saveCurrentPreference(1L, 1L, saveRequest());

        verify(mapper).updateById(existing);
        assertThat(existing.getThemeScheme()).isEqualTo("dark");
        assertThat(existing.getContentWidth()).isEqualTo("fixed");
    }

    private UserPreferenceDO preference() {
        UserPreferenceDO preference = new UserPreferenceDO();
        preference.setId(1L);
        preference.setTenantId(1L);
        preference.setUserId(1L);
        preference.setThemeScheme("dark");
        preference.setThemeColor("#13c2c2");
        preference.setLayoutMode("mix");
        preference.setContentWidth("fixed");
        preference.setTabBar(1);
        preference.setFixedHeader(1);
        preference.setFixedSidebar(1);
        preference.setSidebarCollapsed(1);
        preference.setCompactMode(0);
        preference.setLocale("en-US");
        return preference;
    }

    private UserPreferenceSaveRequest saveRequest() {
        UserPreferenceSaveRequest request = new UserPreferenceSaveRequest();
        request.setThemeScheme("dark");
        request.setThemeColor("#13c2c2");
        request.setLayoutMode("mix");
        request.setContentWidth("fixed");
        request.setTabBar(1);
        request.setFixedHeader(1);
        request.setFixedSidebar(1);
        request.setSidebarCollapsed(1);
        request.setCompactMode(0);
        request.setLocale("en-US");
        return request;
    }
}
