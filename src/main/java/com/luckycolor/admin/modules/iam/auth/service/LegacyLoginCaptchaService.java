package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaChallengeResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaVerifyResponse;

public interface LegacyLoginCaptchaService {

    LegacyLoginCaptchaChallengeResponse createChallenge();

    LegacyLoginCaptchaVerifyResponse verifyChallenge(String captchaId, String answer);

    void validateCaptchaToken(String captchaToken);
}
