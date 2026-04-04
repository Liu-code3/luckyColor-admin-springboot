package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthButtonPermissionsResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRefreshResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import java.util.List;

public interface AuthService {

    AuthLoginResponse login(AuthLoginRequest request);

    AuthRefreshResponse refresh(String refreshToken);

    void logout(JwtAuthenticatedUser authenticatedUser, String accessToken, String refreshToken, String remoteIp);

    AuthProfileResponse getProfile(JwtAuthenticatedUser authenticatedUser);

    AuthPermissionSnapshotResponse getPermissionSnapshot(JwtAuthenticatedUser authenticatedUser);

    AuthButtonPermissionsResponse getButtonPermissions(JwtAuthenticatedUser authenticatedUser, List<String> requestedCodes);

    List<AuthRouteResponse> getRoutes(JwtAuthenticatedUser authenticatedUser);

    AuthAccessSnapshotResponse getAccessSnapshot(JwtAuthenticatedUser authenticatedUser);
}
