package com.luckycolor.admin.modules.iam.auth.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {

    private final AuthService authService;
    private final LoginCaptchaService loginCaptchaService;
    private final LoginCaptchaProperties loginCaptchaProperties;

    public AuthController(
        AuthService authService,
        @Nullable LoginCaptchaService loginCaptchaService,
        LoginCaptchaProperties loginCaptchaProperties
    ) {
        this.authService = authService;
        this.loginCaptchaService = loginCaptchaService;
        this.loginCaptchaProperties = loginCaptchaProperties;
    }

    @GetMapping("/auth/captcha")
    public ApiResponse<LoginCaptchaResponse> captcha() {
        if (!loginCaptchaProperties.isEnabled() || loginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        return ApiResponse.success(loginCaptchaService.createCaptcha());
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthLoginResponse> login(
        @Valid @RequestBody AuthLoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        request.setRemoteIp(httpServletRequest.getRemoteAddr());
        return ApiResponse.success(authService.login(request));
    }
}
