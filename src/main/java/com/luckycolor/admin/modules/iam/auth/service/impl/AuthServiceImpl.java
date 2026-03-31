package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginAuditService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final LocalAuthProperties localAuthProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SecurityJwtProperties securityJwtProperties;
    private final LoginCaptchaProperties loginCaptchaProperties;
    private final LoginAuditService loginAuditService;
    private final LoginCaptchaService loginCaptchaService;

    public AuthServiceImpl(
        LocalAuthProperties localAuthProperties,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        SecurityJwtProperties securityJwtProperties,
        LoginCaptchaProperties loginCaptchaProperties,
        LoginAuditService loginAuditService,
        @Nullable LoginCaptchaService loginCaptchaService
    ) {
        this.localAuthProperties = localAuthProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.securityJwtProperties = securityJwtProperties;
        this.loginCaptchaProperties = loginCaptchaProperties;
        this.loginAuditService = loginAuditService;
        this.loginCaptchaService = loginCaptchaService;
    }

    @Override
    public AuthLoginResponse login(AuthLoginRequest request) {
        validateCaptchaIfNecessary(request);
        LocalAuthProperties.User user = findUser(request.getUsername());
        if (user == null) {
            loginAuditService.recordFailure(request.getUsername(), null, request.getRemoteIp(), "USER_NOT_FOUND");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        if (!Objects.equals(user.getStatus(), 0)) {
            loginAuditService.recordFailure(user.getUsername(), user.getTenantId(), request.getRemoteIp(), "USER_DISABLED");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is disabled");
        }
        if (!matchesPassword(request.getPassword(), user.getPassword())) {
            loginAuditService.recordFailure(user.getUsername(), user.getTenantId(), request.getRemoteIp(), "PASSWORD_MISMATCH");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }

        String accessToken = jwtTokenService.createAccessToken(
            user.getUserId(),
            user.getUsername(),
            user.getTenantId(),
            user.getRoles()
        );
        loginAuditService.recordSuccess(user.getUsername(), user.getTenantId(), request.getRemoteIp());
        return new AuthLoginResponse(
            accessToken,
            "Bearer",
            securityJwtProperties.resolveExpiresIn().toSeconds(),
            user.getUserId(),
            user.getUsername(),
            user.getNickname(),
            user.getTenantId(),
            user.getRoles()
        );
    }

    private void validateCaptchaIfNecessary(AuthLoginRequest request) {
        if (!loginCaptchaProperties.isEnabled()) {
            return;
        }
        if (loginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        loginCaptchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
    }

    private LocalAuthProperties.User findUser(String username) {
        return localAuthProperties.getLocalUsers().stream()
            .filter(item -> StringUtils.hasText(item.getUsername()))
            .filter(item -> item.getUsername().equalsIgnoreCase(username))
            .findFirst()
            .orElse(null);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Objects.equals(rawPassword, storedPassword);
    }
}
