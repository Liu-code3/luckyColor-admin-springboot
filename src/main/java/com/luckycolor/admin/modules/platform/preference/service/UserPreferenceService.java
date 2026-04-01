package com.luckycolor.admin.modules.platform.preference.service;

import com.luckycolor.admin.modules.platform.preference.web.request.UserPreferenceSaveRequest;
import com.luckycolor.admin.modules.platform.preference.web.response.UserPreferenceResponse;

public interface UserPreferenceService {

    UserPreferenceResponse getCurrentPreference(Long userId, Long tenantId);

    void saveCurrentPreference(Long userId, Long tenantId, UserPreferenceSaveRequest request);
}
