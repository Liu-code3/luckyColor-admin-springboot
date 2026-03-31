package com.luckycolor.admin.modules.iam.auth.web.response;

public record LoginCaptchaResponse(
    String captchaKey,
    String captchaImage
) {
}
