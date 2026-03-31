package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;

public interface AuthService {

    AuthLoginResponse login(AuthLoginRequest request);

    void logout(JwtAuthenticatedUser authenticatedUser, String token, String remoteIp);

    AuthProfileResponse getProfile(JwtAuthenticatedUser authenticatedUser);

    AuthPermissionSnapshotResponse getPermissionSnapshot(JwtAuthenticatedUser authenticatedUser);
}
