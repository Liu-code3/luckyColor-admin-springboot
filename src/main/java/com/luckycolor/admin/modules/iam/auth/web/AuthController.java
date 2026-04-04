package com.luckycolor.admin.modules.iam.auth.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LegacyLoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.request.AuthLoginRequest;
import com.luckycolor.admin.modules.iam.auth.web.request.LegacyLoginCaptchaVerifyRequest;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthAccessSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthButtonPermissionsResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthLoginResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthPermissionSnapshotResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthProfileResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRefreshResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.AuthRouteResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaChallengeResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LegacyLoginCaptchaVerifyResponse;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "Authentication", description = "Login and current-session APIs")
public class AuthController {

    private final AuthService authService;
    private final LoginCaptchaService loginCaptchaService;
    private final LegacyLoginCaptchaService legacyLoginCaptchaService;
    private final LoginCaptchaProperties loginCaptchaProperties;
    private final JwtTokenService jwtTokenService;
    private final SecurityJwtProperties securityJwtProperties;

    public AuthController(
        AuthService authService,
        @Nullable LoginCaptchaService loginCaptchaService,
        @Nullable LegacyLoginCaptchaService legacyLoginCaptchaService,
        LoginCaptchaProperties loginCaptchaProperties,
        JwtTokenService jwtTokenService,
        SecurityJwtProperties securityJwtProperties
    ) {
        this.authService = authService;
        this.loginCaptchaService = loginCaptchaService;
        this.legacyLoginCaptchaService = legacyLoginCaptchaService;
        this.loginCaptchaProperties = loginCaptchaProperties;
        this.jwtTokenService = jwtTokenService;
        this.securityJwtProperties = securityJwtProperties;
    }

    @GetMapping("/auth/captcha")
    @Operation(summary = "Get login captcha")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Captcha generated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\":0,\"message\":\"ok\",\"data\":{\"captchaKey\":\"captcha-20260402-001\",\"captchaImage\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...\"},\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "Captcha service unavailable",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\":503,\"message\":\"Login captcha service is unavailable\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
                )
            )
        )
    })
    public ApiResponse<LoginCaptchaResponse> captcha() {
        if (!loginCaptchaProperties.isEnabled() || loginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        return ApiResponse.success(loginCaptchaService.createCaptcha());
    }

    @GetMapping("/auth/captcha/challenge")
    @Operation(summary = "Get legacy arithmetic login captcha challenge")
    public ApiResponse<LegacyLoginCaptchaChallengeResponse> legacyCaptchaChallenge() {
        if (!loginCaptchaProperties.isEnabled() || legacyLoginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        return ApiResponse.success(legacyLoginCaptchaService.createChallenge());
    }

    @PostMapping("/auth/captcha/verify")
    @Operation(summary = "Verify legacy arithmetic login captcha challenge")
    public ApiResponse<LegacyLoginCaptchaVerifyResponse> legacyCaptchaVerify(
        @Valid @RequestBody LegacyLoginCaptchaVerifyRequest request
    ) {
        if (!loginCaptchaProperties.isEnabled() || legacyLoginCaptchaService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Login captcha service is unavailable");
        }
        return ApiResponse.success(legacyLoginCaptchaService.verifyChallenge(request.captchaId(), request.answer()));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Login with username and password")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\":0,\"message\":\"ok\",\"data\":{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9.demo.signature\",\"tokenType\":\"Bearer\",\"expiresIn\":7200,\"userId\":1,\"username\":\"admin\",\"nickname\":\"System Admin\",\"tenantId\":1,\"roles\":[\"ROLE_SUPER_ADMIN\"]},\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation failed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\":400,\"message\":\"username must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Username, password, or captcha incorrect",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = UNAUTHORIZED
                )
            )
        )
    })
    public ApiResponse<AuthLoginResponse> login(
        @Valid @RequestBody AuthLoginRequest request,
        @Parameter(hidden = true) HttpServletRequest httpServletRequest,
        @Parameter(hidden = true) HttpServletResponse httpServletResponse,
        @RequestHeader(value = "x-tenant-id", required = false) String tenantId
    ) {
        request.setRemoteIp(httpServletRequest.getRemoteAddr());
        request.setTenantId(tenantId);
        AuthLoginResponse response = authService.login(request);
        writeRefreshCookie(httpServletResponse, response.refreshToken());
        return ApiResponse.success(response);
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Refresh current session")
    public ApiResponse<AuthRefreshResponse> refresh(
        @Parameter(hidden = true) HttpServletRequest httpServletRequest,
        @Parameter(hidden = true) HttpServletResponse httpServletResponse
    ) {
        AuthRefreshResponse response = authService.refresh(extractRefreshToken(httpServletRequest));
        writeRefreshCookie(httpServletResponse, response.refreshToken());
        return ApiResponse.success(response);
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Logout current session")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\":0,\"message\":\"ok\",\"data\":true,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = UNAUTHORIZED
                )
            )
        )
    })
    public ApiResponse<Boolean> logout(
        @Parameter(hidden = true) Authentication authentication,
        @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @Parameter(hidden = true) HttpServletRequest httpServletRequest,
        @Parameter(hidden = true) HttpServletResponse httpServletResponse
    ) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        authService.logout(
            authenticatedUser,
            jwtTokenService.resolveBearerToken(authorizationHeader),
            extractRefreshToken(httpServletRequest),
            httpServletRequest.getRemoteAddr()
        );
        clearRefreshCookie(httpServletResponse);
        return ApiResponse.success(true);
    }

    @GetMapping("/auth/profile")
    @Operation(summary = "Get current user profile")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        )
    })
    public ApiResponse<AuthProfileResponse> profile(@Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.success(authService.getProfile(getAuthenticatedUser(authentication)));
    }

    @GetMapping("/auth/permissions")
    @Operation(summary = "Get current user permissions")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission snapshot loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        )
    })
    public ApiResponse<AuthPermissionSnapshotResponse> permissions(@Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.success(authService.getPermissionSnapshot(getAuthenticatedUser(authentication)));
    }

    @GetMapping("/auth/button-permissions")
    @Operation(summary = "Get current user button permissions")
    public ApiResponse<AuthButtonPermissionsResponse> buttonPermissions(
        @Parameter(hidden = true) Authentication authentication,
        @RequestParam(value = "codes", required = false) List<String> codes
    ) {
        return ApiResponse.success(authService.getButtonPermissions(getAuthenticatedUser(authentication), normalizeCodes(codes)));
    }

    @GetMapping("/auth/routes")
    @Operation(summary = "Get current user routes")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Route list loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        )
    })
    public ApiResponse<List<AuthRouteResponse>> routes(@Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.success(authService.getRoutes(getAuthenticatedUser(authentication)));
    }

    @GetMapping("/auth/access")
    @Operation(summary = "Get current user access snapshot")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Access snapshot loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        )
    })
    public ApiResponse<AuthAccessSnapshotResponse> access(@Parameter(hidden = true) Authentication authentication) {
        return ApiResponse.success(authService.getAccessSnapshot(getAuthenticatedUser(authentication)));
    }

    private JwtAuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        jakarta.servlet.http.Cookie cookie = WebUtils.getCookie(request, securityJwtProperties.getRefreshCookieName());
        return cookie == null ? null : cookie.getValue();
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        if (response == null || refreshToken == null) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(securityJwtProperties.getRefreshCookieName(), refreshToken)
            .httpOnly(true)
            .secure(securityJwtProperties.isRefreshCookieSecure())
            .sameSite("Lax")
            .path(securityJwtProperties.getRefreshCookiePath())
            .maxAge(securityJwtProperties.resolveRefreshExpiresIn())
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(securityJwtProperties.getRefreshCookieName(), "")
            .httpOnly(true)
            .secure(securityJwtProperties.isRefreshCookieSecure())
            .sameSite("Lax")
            .path(securityJwtProperties.getRefreshCookiePath())
            .maxAge(0)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return codes.stream()
            .filter(value -> value != null && !value.isBlank())
            .flatMap(value -> List.of(value.split(",")).stream())
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .distinct()
            .toList();
    }
}
