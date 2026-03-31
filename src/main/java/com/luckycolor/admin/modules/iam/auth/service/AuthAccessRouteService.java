package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import java.util.List;

public interface AuthAccessRouteService {

    List<AuthRouteResponse> getAccessibleRoutes(AuthUser user);

    AuthAccessSnapshotResponse getAccessSnapshot(AuthUser user);
}
