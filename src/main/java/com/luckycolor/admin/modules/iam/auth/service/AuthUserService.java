package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.modules.iam.auth.model.AuthUser;

public interface AuthUserService {

    AuthUser findByUsername(String username);

    AuthUser getByUserId(Long userId);
}
