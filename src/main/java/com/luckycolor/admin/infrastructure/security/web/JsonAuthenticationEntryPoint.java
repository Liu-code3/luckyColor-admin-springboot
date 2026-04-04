package com.luckycolor.admin.infrastructure.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.error.ApiErrorCode;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final SecurityAuditLogService securityAuditLogService;

    public JsonAuthenticationEntryPoint(
        ObjectMapper objectMapper,
        @Nullable SecurityAuditLogService securityAuditLogService
    ) {
        this.objectMapper = objectMapper;
        this.securityAuditLogService = securityAuditLogService;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {
        String reason = resolveReason(request, authException);
        if (securityAuditLogService != null) {
            try {
                securityAuditLogService.recordUnauthorized(request, reason);
            } catch (RuntimeException ignored) {
                // Authentication failure response must not be blocked by optional audit persistence.
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(resolveErrorCode(reason), resolveMessage(reason)));
    }

    private String resolveReason(HttpServletRequest request, AuthenticationException authException) {
        Object requestReason = request.getAttribute(SecurityRequestAttributes.AUTH_FAILURE_REASON);
        if (requestReason instanceof String value && StringUtils.hasText(value)) {
            return value;
        }
        if (authException != null && StringUtils.hasText(authException.getMessage())) {
            return authException.getMessage();
        }
        return "UNAUTHORIZED";
    }

    private int resolveErrorCode(String reason) {
        return switch (reason) {
            case "TOKEN_EXPIRED" -> ApiErrorCode.AUTH_TOKEN_EXPIRED;
            case "TOKEN_INVALID", "TOKEN_REVOKED" -> ApiErrorCode.AUTH_TOKEN_INVALID;
            default -> ApiErrorCode.UNAUTHORIZED;
        };
    }

    private String resolveMessage(String reason) {
        return switch (reason) {
            case "TOKEN_EXPIRED" -> "access token expired, please sign in again";
            case "TOKEN_INVALID", "TOKEN_REVOKED" -> "access token invalid, please sign in again";
            default -> "Unauthorized";
        };
    }
}
