package com.luckycolor.admin.modules.platform.preference.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.modules.platform.preference.dataobject.UserPreferenceDO;
import com.luckycolor.admin.modules.platform.preference.mapper.UserPreferenceMapper;
import com.luckycolor.admin.modules.platform.preference.service.UserPreferenceService;
import com.luckycolor.admin.modules.platform.preference.web.request.UserPreferenceSaveRequest;
import com.luckycolor.admin.modules.platform.preference.web.response.UserPreferenceResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(UserPreferenceMapper.class)
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private static final String DEFAULT_THEME_SCHEME = "light";
    private static final String DEFAULT_THEME_COLOR = "#1677ff";
    private static final String DEFAULT_LAYOUT_MODE = "side";
    private static final String DEFAULT_CONTENT_WIDTH = "fluid";
    private static final int DEFAULT_TAB_BAR = 1;
    private static final int DEFAULT_FIXED_HEADER = 1;
    private static final int DEFAULT_FIXED_SIDEBAR = 1;
    private static final int DEFAULT_SIDEBAR_COLLAPSED = 0;
    private static final int DEFAULT_COMPACT_MODE = 0;
    private static final String DEFAULT_LOCALE = "zh-CN";

    private final UserPreferenceMapper userPreferenceMapper;

    public UserPreferenceServiceImpl(UserPreferenceMapper userPreferenceMapper) {
        this.userPreferenceMapper = userPreferenceMapper;
    }

    @Override
    public UserPreferenceResponse getCurrentPreference(Long userId, Long tenantId) {
        UserPreferenceDO preference = findByUser(userId, tenantId);
        if (preference == null) {
            return buildDefaultPreference(userId, tenantId);
        }
        return toResponse(preference);
    }

    @Override
    public void saveCurrentPreference(Long userId, Long tenantId, UserPreferenceSaveRequest request) {
        UserPreferenceDO preference = findByUser(userId, tenantId);
        if (preference == null) {
            preference = new UserPreferenceDO();
            preference.setUserId(userId);
            preference.setTenantId(tenantId);
            fillPreference(preference, request);
            userPreferenceMapper.insert(preference);
            return;
        }
        fillPreference(preference, request);
        userPreferenceMapper.updateById(preference);
    }

    private UserPreferenceDO findByUser(Long userId, Long tenantId) {
        LambdaQueryWrapper<UserPreferenceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPreferenceDO::getUserId, userId);
        if (tenantId == null) {
            queryWrapper.isNull(UserPreferenceDO::getTenantId);
        } else {
            queryWrapper.eq(UserPreferenceDO::getTenantId, tenantId);
        }
        return userPreferenceMapper.selectOne(queryWrapper);
    }

    private void fillPreference(UserPreferenceDO preference, UserPreferenceSaveRequest request) {
        preference.setThemeScheme(trim(request.getThemeScheme()));
        preference.setThemeColor(trim(request.getThemeColor()));
        preference.setLayoutMode(trim(request.getLayoutMode()));
        preference.setContentWidth(trim(request.getContentWidth()));
        preference.setTabBar(request.getTabBar());
        preference.setFixedHeader(request.getFixedHeader());
        preference.setFixedSidebar(request.getFixedSidebar());
        preference.setSidebarCollapsed(request.getSidebarCollapsed());
        preference.setCompactMode(request.getCompactMode());
        preference.setLocale(trim(request.getLocale()));
    }

    private UserPreferenceResponse buildDefaultPreference(Long userId, Long tenantId) {
        return new UserPreferenceResponse(
            userId,
            tenantId,
            DEFAULT_THEME_SCHEME,
            DEFAULT_THEME_COLOR,
            DEFAULT_LAYOUT_MODE,
            DEFAULT_CONTENT_WIDTH,
            DEFAULT_TAB_BAR,
            DEFAULT_FIXED_HEADER,
            DEFAULT_FIXED_SIDEBAR,
            DEFAULT_SIDEBAR_COLLAPSED,
            DEFAULT_COMPACT_MODE,
            DEFAULT_LOCALE
        );
    }

    private UserPreferenceResponse toResponse(UserPreferenceDO preference) {
        return new UserPreferenceResponse(
            preference.getUserId(),
            preference.getTenantId(),
            preference.getThemeScheme(),
            preference.getThemeColor(),
            preference.getLayoutMode(),
            preference.getContentWidth(),
            preference.getTabBar(),
            preference.getFixedHeader(),
            preference.getFixedSidebar(),
            preference.getSidebarCollapsed(),
            preference.getCompactMode(),
            preference.getLocale()
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
