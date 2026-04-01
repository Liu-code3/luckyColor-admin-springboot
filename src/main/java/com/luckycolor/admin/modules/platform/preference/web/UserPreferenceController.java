package com.luckycolor.admin.modules.platform.preference.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.preference.mapper.UserPreferenceMapper;
import com.luckycolor.admin.modules.platform.preference.service.UserPreferenceService;
import com.luckycolor.admin.modules.platform.preference.web.request.UserPreferenceSaveRequest;
import com.luckycolor.admin.modules.platform.preference.web.response.UserPreferenceResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/user-preferences")
@Validated
@ConditionalOnBean(UserPreferenceMapper.class)
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping("/current")
    public ApiResponse<UserPreferenceResponse> getCurrent(Authentication authentication) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        return ApiResponse.success(
            userPreferenceService.getCurrentPreference(authenticatedUser.userId(), authenticatedUser.tenantId())
        );
    }

    @PutMapping("/current")
    public ApiResponse<Boolean> saveCurrent(
        Authentication authentication,
        @Valid @RequestBody UserPreferenceSaveRequest request
    ) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        userPreferenceService.saveCurrentPreference(authenticatedUser.userId(), authenticatedUser.tenantId(), request);
        return ApiResponse.success(true);
    }

    private JwtAuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }
}
