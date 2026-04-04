package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtRefreshTokenClaims;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.infrastructure.tenant.service.TenantExternalIdService;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthAccessRouteService;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.service.LegacyLoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.service.AuthTokenSessionService;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import com.luckycolor.admin.modules.iam.auth.service.LoginAuditService;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthButtonPermissionsResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginUserResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRefreshResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthUserService authUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthTokenSessionService authTokenSessionService;
    private final AuthAccessRouteService authAccessRouteService;
    private final SecurityJwtProperties securityJwtProperties;
    private final LoginCaptchaProperties loginCaptchaProperties;
    private final LoginAuditService loginAuditService;
    private final LoginCaptchaService loginCaptchaService;
    private final LegacyLoginCaptchaService legacyLoginCaptchaService;
    private final SecurityAuditLogService securityAuditLogService;
    private final TenantExternalIdService tenantExternalIdService;

    public AuthServiceImpl(
        AuthUserService authUserService,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        AuthTokenSessionService authTokenSessionService,
        AuthAccessRouteService authAccessRouteService,
        SecurityJwtProperties securityJwtProperties,
        LoginCaptchaProperties loginCaptchaProperties,
        LoginAuditService loginAuditService,
        @Nullable SecurityAuditLogService securityAuditLogService,
        @Nullable LoginCaptchaService loginCaptchaService,
        @Nullable LegacyLoginCaptchaService legacyLoginCaptchaService,
        TenantExternalIdService tenantExternalIdService
    ) {
        this.authUserService = authUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authTokenSessionService = authTokenSessionService;
        this.authAccessRouteService = authAccessRouteService;
        this.securityJwtProperties = securityJwtProperties;
        this.loginCaptchaProperties = loginCaptchaProperties;
        this.loginAuditService = loginAuditService;
        this.securityAuditLogService = securityAuditLogService;
        this.loginCaptchaService = loginCaptchaService;
        this.legacyLoginCaptchaService = legacyLoginCaptchaService;
        this.tenantExternalIdService = tenantExternalIdService;
    }

    @Override
    public AuthLoginResponse login(AuthLoginRequest request) {
        validateCaptchaIfNecessary(request);
        AuthUser user = authUserService.findByUsername(request.getUsername(), request.getTenantId());
        if (user == null) {
            loginAuditService.recordFailure(null, request.getUsername(), null, request.getRemoteIp(), "USER_NOT_FOUND");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_FAILED");
        }
        if (!Objects.equals(user.status(), 0)) {
            loginAuditService.recordFailure(
                user.userId(),
                user.username(),
                user.tenantId(),
                request.getRemoteIp(),
                "USER_DISABLED"
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_DISABLED");
        }
        if (!matchesPassword(request.getPassword(), user.password())) {
            loginAuditService.recordFailure(
                user.userId(),
                user.username(),
                user.tenantId(),
                request.getRemoteIp(),
                "PASSWORD_MISMATCH"
            );
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_FAILED");
        }

        String accessToken = jwtTokenService.createAccessToken(
            user.userId(),
            user.username(),
            user.tenantId(),
            user.roles()
        );
        String refreshToken = jwtTokenService.createRefreshToken(user.userId(), user.username(), user.tenantId());
        AuthAccessSnapshotResponse accessSnapshot = authAccessRouteService.getAccessSnapshot(user);
        List<String> roleCodes = resolveRoleCodes(user, accessSnapshot);
        List<String> menuCodes = resolveMenuCodes(accessSnapshot);
        List<String> buttonCodes = resolveButtonCodes(user, accessSnapshot);
        loginAuditService.recordSuccess(user.userId(), user.username(), user.tenantId(), request.getRemoteIp());
        return new AuthLoginResponse(
            accessToken,
            "Bearer",
            securityJwtProperties.resolveExpiresIn().toSeconds(),
            refreshToken,
            user.userId(),
            user.username(),
            user.nickname(),
            tenantExternalIdService.toExternalTenantId(user.tenantId()),
            roleCodes,
            buttonCodes,
            buttonCodes,
            buttonCodes,
            buttonCodes,
            menuCodes,
            user.dataScope(),
            resolveDataScopeDeptIds(user),
            new AuthLoginUserResponse(
                user.userId(),
                tenantExternalIdService.toExternalTenantId(user.tenantId()),
                null,
                user.username(),
                user.nickname(),
                roleCodes,
                menuCodes,
                buttonCodes,
                buttonCodes,
                buttonCodes,
                buttonCodes,
                user.dataScope(),
                resolveDataScopeDeptIds(user)
            )
        );
    }

    @Override
    public AuthRefreshResponse refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID");
        }
        if (authTokenSessionService.isRevoked(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID");
        }
        JwtRefreshTokenClaims claims;
        try {
            claims = jwtTokenService.parseRefreshToken(refreshToken);
        } catch (ExpiredJwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_EXPIRED", exception);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID", exception);
        }
        AuthUser user = authUserService.getByUserId(claims.userId());
        if (user == null || !Objects.equals(user.status(), 0)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID");
        }
        authTokenSessionService.revoke(refreshToken, jwtTokenService.resolveRefreshExpiration(refreshToken));
        return new AuthRefreshResponse(
            jwtTokenService.createAccessToken(user.userId(), user.username(), user.tenantId(), user.roles()),
            "Bearer",
            securityJwtProperties.resolveExpiresIn().toSeconds(),
            jwtTokenService.createRefreshToken(user.userId(), user.username(), user.tenantId())
        );
    }

    @Override
    public void logout(JwtAuthenticatedUser authenticatedUser, String accessToken, String refreshToken, String remoteIp) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (!StringUtils.hasText(accessToken) && !StringUtils.hasText(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bearer token is required");
        }
        if (StringUtils.hasText(accessToken)) {
            Instant expiresAt = jwtTokenService.resolveExpiration(accessToken);
            authTokenSessionService.revoke(accessToken, expiresAt);
        }
        if (StringUtils.hasText(refreshToken)) {
            Instant expiresAt = jwtTokenService.resolveRefreshExpiration(refreshToken);
            authTokenSessionService.revoke(refreshToken, expiresAt);
        }
        if (securityAuditLogService != null) {
            try {
                securityAuditLogService.recordLogout(
                    authenticatedUser.userId(),
                    authenticatedUser.username(),
                    authenticatedUser.tenantId(),
                    remoteIp
                );
            } catch (RuntimeException exception) {
                log.warn(
                    "failed to persist logout audit userId={} tenantId={}",
                    authenticatedUser.userId(),
                    authenticatedUser.tenantId(),
                    exception
                );
            }
        }
    }

    @Override
    public AuthProfileResponse getProfile(JwtAuthenticatedUser authenticatedUser) {
        AuthUser user = getRequiredUser(authenticatedUser);
        AuthAccessSnapshotResponse accessSnapshot = authAccessRouteService.getAccessSnapshot(user);
        List<String> roleCodes = resolveRoleCodes(user, accessSnapshot);
        List<String> menuCodes = resolveMenuCodes(accessSnapshot);
        List<String> buttonCodes = resolveButtonCodes(user, accessSnapshot);
        return new AuthProfileResponse(
            user.userId(),
            user.userId(),
            user.username(),
            user.nickname(),
            tenantExternalIdService.toExternalTenantId(user.tenantId()),
            null,
            roleCodes,
            roleCodes,
            menuCodes,
            buttonCodes,
            buttonCodes,
            buttonCodes,
            buttonCodes,
            user.dataScope(),
            resolveDataScopeDeptIds(user)
        );
    }

    @Override
    public AuthPermissionSnapshotResponse getPermissionSnapshot(JwtAuthenticatedUser authenticatedUser) {
        AuthUser user = getRequiredUser(authenticatedUser);
        AuthAccessSnapshotResponse accessSnapshot = authAccessRouteService.getAccessSnapshot(user);
        return new AuthPermissionSnapshotResponse(
            user.userId(),
            tenantExternalIdService.toExternalTenantId(user.tenantId()),
            resolveRoleCodes(user, accessSnapshot),
            resolveButtonCodes(user, accessSnapshot)
        );
    }

    @Override
    public AuthButtonPermissionsResponse getButtonPermissions(
        JwtAuthenticatedUser authenticatedUser,
        List<String> requestedCodes
    ) {
        AuthUser user = getRequiredUser(authenticatedUser);
        AuthAccessSnapshotResponse accessSnapshot = authAccessRouteService.getAccessSnapshot(user);
        List<String> buttonCodes = resolveButtonCodes(user, accessSnapshot);
        List<String> targetCodes = requestedCodes == null || requestedCodes.isEmpty() ? buttonCodes : requestedCodes;
        Map<String, Boolean> permissionMap = new LinkedHashMap<>();
        LinkedHashSet<String> grantedCodes = new LinkedHashSet<>();
        for (String code : targetCodes) {
            boolean granted = buttonCodes.contains(code);
            permissionMap.put(code, granted);
            if (granted) {
                grantedCodes.add(code);
            }
        }
        return new AuthButtonPermissionsResponse(buttonCodes, List.copyOf(grantedCodes), permissionMap);
    }

    @Override
    public List<AuthRouteResponse> getRoutes(JwtAuthenticatedUser authenticatedUser) {
        return authAccessRouteService.getAccessibleRoutes(getRequiredUser(authenticatedUser));
    }

    @Override
    public AuthAccessSnapshotResponse getAccessSnapshot(JwtAuthenticatedUser authenticatedUser) {
        return authAccessRouteService.getAccessSnapshot(getRequiredUser(authenticatedUser));
    }

    private void validateCaptchaIfNecessary(AuthLoginRequest request) {
        if (!loginCaptchaProperties.isEnabled()) {
            return;
        }
        if (StringUtils.hasText(request.getCaptchaToken())) {
            if (legacyLoginCaptchaService == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
            }
            legacyLoginCaptchaService.validateCaptchaToken(request.getCaptchaToken());
            return;
        }
        if (loginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        loginCaptchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
    }

    private List<Long> resolveDataScopeDeptIds(AuthUser user) {
        if (user.departmentIds() != null && !user.departmentIds().isEmpty()) {
            return user.departmentIds();
        }
        if (user.departmentId() != null) {
            return List.of(user.departmentId());
        }
        return List.of();
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Objects.equals(rawPassword, storedPassword);
    }

    private List<String> resolveRoleCodes(AuthUser user, AuthAccessSnapshotResponse accessSnapshot) {
        if (accessSnapshot != null && accessSnapshot.user() != null && accessSnapshot.user().roleCodes() != null) {
            return accessSnapshot.user().roleCodes();
        }
        return user.roles();
    }

    private List<String> resolveButtonCodes(AuthUser user, AuthAccessSnapshotResponse accessSnapshot) {
        if (accessSnapshot != null && accessSnapshot.user() != null && accessSnapshot.user().buttonCodeList() != null) {
            return accessSnapshot.user().buttonCodeList();
        }
        return user.permissions();
    }

    private List<String> resolveMenuCodes(AuthAccessSnapshotResponse accessSnapshot) {
        if (accessSnapshot != null && accessSnapshot.user() != null && accessSnapshot.user().menuCodeList() != null) {
            return accessSnapshot.user().menuCodeList();
        }
        return List.of();
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
