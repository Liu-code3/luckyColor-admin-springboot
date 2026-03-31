package com.luckycolor.admin.modules.iam.auth.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(LoginCaptchaService.class)
public class AuthController {

    private final LoginCaptchaService loginCaptchaService;

    public AuthController(LoginCaptchaService loginCaptchaService) {
        this.loginCaptchaService = loginCaptchaService;
    }

    @GetMapping("/auth/captcha")
    public ApiResponse<LoginCaptchaResponse> captcha() {
        return ApiResponse.success(loginCaptchaService.createCaptcha());
    }
}
