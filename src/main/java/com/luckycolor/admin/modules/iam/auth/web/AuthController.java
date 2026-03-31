package com.luckycolor.admin.modules.iam.auth.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {

    private final AuthService authService;
    private final LoginCaptchaService loginCaptchaService;
    private final LoginCaptchaProperties loginCaptchaProperties;
    private final JwtTokenService jwtTokenService;

    public AuthController(
        AuthService authService,
        @Nullable LoginCaptchaService loginCaptchaService,
        LoginCaptchaProperties loginCaptchaProperties,
        JwtTokenService jwtTokenService
    ) {
        this.authService = authService;
        this.loginCaptchaService = loginCaptchaService;
        this.loginCaptchaProperties = loginCaptchaProperties;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/auth/captcha")
    public ApiResponse<LoginCaptchaResponse> captcha() {
        if (!loginCaptchaProperties.isEnabled() || loginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        return ApiResponse.success(loginCaptchaService.createCaptcha());
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthLoginResponse> login(
        @Valid @RequestBody AuthLoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        request.setRemoteIp(httpServletRequest.getRemoteAddr());
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Boolean> logout(
        Authentication authentication,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        HttpServletRequest httpServletRequest
    ) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        authService.logout(
            authenticatedUser,
            jwtTokenService.resolveBearerToken(authorizationHeader),
            httpServletRequest.getRemoteAddr()
        );
        return ApiResponse.success(true);
    }

    @GetMapping("/auth/profile")
    public ApiResponse<AuthProfileResponse> profile(Authentication authentication) {
        return ApiResponse.success(authService.getProfile(getAuthenticatedUser(authentication)));
    }

    @GetMapping("/auth/permissions")
    public ApiResponse<AuthPermissionSnapshotResponse> permissions(Authentication authentication) {
        return ApiResponse.success(authService.getPermissionSnapshot(getAuthenticatedUser(authentication)));
    }

    @GetMapping("/auth/routes")
    public ApiResponse<List<AuthRouteResponse>> routes(Authentication authentication) {
        return ApiResponse.success(authService.getRoutes(getAuthenticatedUser(authentication)));
    }

    @GetMapping("/auth/access")
    public ApiResponse<AuthAccessSnapshotResponse> access(Authentication authentication) {
        return ApiResponse.success(authService.getAccessSnapshot(getAuthenticatedUser(authentication)));
    }

    private JwtAuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }
}
