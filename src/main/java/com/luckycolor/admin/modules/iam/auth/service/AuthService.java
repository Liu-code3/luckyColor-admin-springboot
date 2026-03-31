package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;

public interface AuthService {

    AuthLoginResponse login(AuthLoginRequest request);
}
