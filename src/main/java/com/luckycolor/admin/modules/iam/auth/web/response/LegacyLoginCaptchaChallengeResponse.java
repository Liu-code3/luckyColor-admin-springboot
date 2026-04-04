package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Legacy arithmetic captcha challenge payload")
public record LegacyLoginCaptchaChallengeResponse(
    @Schema(description = "Captcha challenge ID", example = "captcha-challenge-demo")
    String captchaId,
    @Schema(description = "Inline SVG markup for arithmetic captcha")
    String captchaSvg,
    @Schema(description = "Captcha prompt", example = "请计算结果")
    String prompt,
    @Schema(description = "Captcha expiration time", example = "2026-04-02T13:20:55.744567200Z")
    Instant expiresAt
) {
}
