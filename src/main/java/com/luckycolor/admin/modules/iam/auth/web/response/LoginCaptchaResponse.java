package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login captcha payload")
public record LoginCaptchaResponse(
    @Schema(description = "Captcha key", example = "captcha-20260402-001")
    String captchaKey,
    @Schema(description = "Captcha image as base64 data URL", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    String captchaImage
) {
}
