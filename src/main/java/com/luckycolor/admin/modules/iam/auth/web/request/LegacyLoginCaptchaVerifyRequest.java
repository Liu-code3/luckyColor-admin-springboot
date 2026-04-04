package com.luckycolor.admin.modules.iam.auth.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Legacy arithmetic captcha verify request")
public record LegacyLoginCaptchaVerifyRequest(
    @NotBlank
    @Schema(description = "Captcha challenge ID", example = "captcha-challenge-demo")
    String captchaId,
    @NotBlank
    @Schema(description = "Arithmetic captcha answer", example = "12")
    String answer
) {
}
