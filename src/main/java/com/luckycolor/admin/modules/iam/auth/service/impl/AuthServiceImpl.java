package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginAuditService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.service.AuthTokenSessionService;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthUserService authUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthTokenSessionService authTokenSessionService;
    private final SecurityJwtProperties securityJwtProperties;
    private final LoginCaptchaProperties loginCaptchaProperties;
    private final LoginAuditService loginAuditService;
    private final LoginCaptchaService loginCaptchaService;

    public AuthServiceImpl(
        AuthUserService authUserService,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        AuthTokenSessionService authTokenSessionService,
        SecurityJwtProperties securityJwtProperties,
        LoginCaptchaProperties loginCaptchaProperties,
        LoginAuditService loginAuditService,
        @Nullable LoginCaptchaService loginCaptchaService
    ) {
        this.authUserService = authUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authTokenSessionService = authTokenSessionService;
        this.securityJwtProperties = securityJwtProperties;
        this.loginCaptchaProperties = loginCaptchaProperties;
        this.loginAuditService = loginAuditService;
        this.loginCaptchaService = loginCaptchaService;
    }

    @Override
    public AuthLoginResponse login(AuthLoginRequest request) {
        validateCaptchaIfNecessary(request);
        AuthUser user = authUserService.findByUsername(request.getUsername());
        if (user == null) {
            loginAuditService.recordFailure(request.getUsername(), null, request.getRemoteIp(), "USER_NOT_FOUND");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }
        if (!Objects.equals(user.status(), 0)) {
            loginAuditService.recordFailure(user.username(), user.tenantId(), request.getRemoteIp(), "USER_DISABLED");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is disabled");
        }
        if (!matchesPassword(request.getPassword(), user.password())) {
            loginAuditService.recordFailure(user.username(), user.tenantId(), request.getRemoteIp(), "PASSWORD_MISMATCH");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }

        String accessToken = jwtTokenService.createAccessToken(
            user.userId(),
            user.username(),
            user.tenantId(),
            user.roles()
        );
        loginAuditService.recordSuccess(user.username(), user.tenantId(), request.getRemoteIp());
        return new AuthLoginResponse(
            accessToken,
            "Bearer",
            securityJwtProperties.resolveExpiresIn().toSeconds(),
            user.userId(),
            user.username(),
            user.nickname(),
            user.tenantId(),
            user.roles()
        );
    }

    @Override
    public void logout(JwtAuthenticatedUser authenticatedUser, String token, String remoteIp) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bearer token is required");
        }
        Instant expiresAt = jwtTokenService.resolveExpiration(token);
        authTokenSessionService.revoke(token, expiresAt);
        loginAuditService.recordSuccess(authenticatedUser.username(), authenticatedUser.tenantId(), remoteIp);
    }

    @Override
    public AuthProfileResponse getProfile(JwtAuthenticatedUser authenticatedUser) {
        AuthUser user = getRequiredUser(authenticatedUser);
        return new AuthProfileResponse(
            user.userId(),
            user.username(),
            user.nickname(),
            user.tenantId(),
            user.roles()
        );
    }

    @Override
    public AuthPermissionSnapshotResponse getPermissionSnapshot(JwtAuthenticatedUser authenticatedUser) {
        AuthUser user = getRequiredUser(authenticatedUser);
        return new AuthPermissionSnapshotResponse(
            user.userId(),
            user.tenantId(),
            user.roles(),
            user.permissions()
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

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Objects.equals(rawPassword, storedPassword);
    }

    private AuthUser getRequiredUser(JwtAuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        AuthUser user = authUserService.getByUserId(authenticatedUser.userId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }
}
