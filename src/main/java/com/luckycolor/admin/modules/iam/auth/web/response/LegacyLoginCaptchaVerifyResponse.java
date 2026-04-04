package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Legacy arithmetic captcha verify response")
public record LegacyLoginCaptchaVerifyResponse(
    @Schema(description = "One-time captcha pass token", example = "captcha-pass-token-demo")
    String captchaToken,
    @Schema(description = "Captcha token expiration time", example = "2026-04-02T13:20:55.744567200Z")
    Instant expiresAt
) {
}
