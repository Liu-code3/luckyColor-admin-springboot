package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LocalAuthUserServiceImpl implements AuthUserService {

    private final LocalAuthProperties localAuthProperties;

    public LocalAuthUserServiceImpl(LocalAuthProperties localAuthProperties) {
        this.localAuthProperties = localAuthProperties;
    }

    @Override
    public AuthUser findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return localAuthProperties.getLocalUsers().stream()
            .filter(item -> StringUtils.hasText(item.getUsername()))
            .filter(item -> item.getUsername().equalsIgnoreCase(username))
            .findFirst()
            .map(this::toAuthUser)
            .orElse(null);
    }

    @Override
    public AuthUser getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return localAuthProperties.getLocalUsers().stream()
            .filter(item -> userId.equals(item.getUserId()))
            .findFirst()
            .map(this::toAuthUser)
            .orElse(null);
    }

    private AuthUser toAuthUser(LocalAuthProperties.User user) {
        return new AuthUser(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            user.getTenantId(),
            user.getNickname(),
            user.getStatus(),
            safeList(user.getRoles()),
            safeList(user.getPermissions())
        );
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
