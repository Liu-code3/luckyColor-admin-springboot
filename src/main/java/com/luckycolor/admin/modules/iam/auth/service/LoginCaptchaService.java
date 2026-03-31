package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;

public interface LoginCaptchaService {

    LoginCaptchaResponse createCaptcha();

    void validateCaptcha(String captchaKey, String captchaCode);
}
