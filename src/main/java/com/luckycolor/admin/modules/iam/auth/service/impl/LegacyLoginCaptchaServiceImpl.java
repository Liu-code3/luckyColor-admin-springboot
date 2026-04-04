package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.LegacyLoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaChallengeResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaVerifyResponse;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LegacyLoginCaptchaServiceImpl implements LegacyLoginCaptchaService {

    private static final String SVG_TEMPLATE = """
        <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d">
          <rect width="100%%" height="100%%" rx="10" fill="#f3f4f6"/>
          <path d="M8 10 L112 34" stroke="#cbd5e1" stroke-width="2" />
          <path d="M14 34 L106 12" stroke="#94a3b8" stroke-width="2" />
          <text x="50%%" y="56%%" text-anchor="middle" dominant-baseline="middle"
                font-family="'Trebuchet MS', 'Microsoft YaHei', sans-serif"
                font-size="22" font-weight="700" letter-spacing="1"
                fill="#1f2937">%s</text>
        </svg>
        """;

    private final LoginCaptchaProperties loginCaptchaProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, PendingChallenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, Instant> passedTokens = new ConcurrentHashMap<>();

    public LegacyLoginCaptchaServiceImpl(LoginCaptchaProperties loginCaptchaProperties) {
        this.loginCaptchaProperties = loginCaptchaProperties;
    }

    @Override
    public LegacyLoginCaptchaChallengeResponse createChallenge() {
        ensureEnabled();
        cleanupExpired();

        ArithmeticExpression expression = createExpression();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(loginCaptchaProperties.getExpireIn());
        challenges.put(captchaId, new PendingChallenge(String.valueOf(expression.answer()), expiresAt));

        return new LegacyLoginCaptchaChallengeResponse(
            captchaId,
            buildSvg(expression.expression()),
            "请计算结果",
            expiresAt
        );
    }

    @Override
    public LegacyLoginCaptchaVerifyResponse verifyChallenge(String captchaId, String answer) {
        ensureEnabled();
        cleanupExpired();

        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(answer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha answer is required");
        }

        PendingChallenge challenge = challenges.remove(captchaId.trim());
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha has expired");
        }

        if (!challenge.answer().equals(answer.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha is invalid");
        }

        String captchaToken = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(loginCaptchaProperties.getExpireIn());
        passedTokens.put(captchaToken, expiresAt);
        return new LegacyLoginCaptchaVerifyResponse(captchaToken, expiresAt);
    }

    @Override
    public void validateCaptchaToken(String captchaToken) {
        ensureEnabled();
        cleanupExpired();

        if (!StringUtils.hasText(captchaToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha token is required");
        }

        Instant expiresAt = passedTokens.remove(captchaToken.trim());
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Captcha token is invalid");
        }
    }

    private void ensureEnabled() {
        if (!loginCaptchaProperties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        passedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    private ArithmeticExpression createExpression() {
        int left = secureRandom.nextInt(9) + 1;
        int right = secureRandom.nextInt(9) + 1;
        if (secureRandom.nextBoolean()) {
            return new ArithmeticExpression(left + " + " + right + " = ?", left + right);
        }

        int max = Math.max(left, right);
        int min = Math.min(left, right);
        return new ArithmeticExpression(max + " - " + min + " = ?", max - min);
    }

    private String buildSvg(String expression) {
        return SVG_TEMPLATE.formatted(
            loginCaptchaProperties.getWidth(),
            loginCaptchaProperties.getHeight(),
            loginCaptchaProperties.getWidth(),
            loginCaptchaProperties.getHeight(),
            expression
        );
    }

    private record PendingChallenge(String answer, Instant expiresAt) {
    }

    private record ArithmeticExpression(String expression, int answer) {
    }
}
