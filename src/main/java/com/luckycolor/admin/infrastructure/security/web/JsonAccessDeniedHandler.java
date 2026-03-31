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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final SecurityAuditLogService securityAuditLogService;

    public JsonAccessDeniedHandler(
        ObjectMapper objectMapper,
        @Nullable SecurityAuditLogService securityAuditLogService
    ) {
        this.objectMapper = objectMapper;
        this.securityAuditLogService = securityAuditLogService;
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        if (securityAuditLogService != null) {
            securityAuditLogService.recordAccessDenied(
                request,
                SecurityContextHolder.getContext().getAuthentication(),
                resolveReason(accessDeniedException)
            );
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(ApiErrorCode.FORBIDDEN, "Forbidden"));
    }

    private String resolveReason(AccessDeniedException accessDeniedException) {
        if (accessDeniedException != null && StringUtils.hasText(accessDeniedException.getMessage())) {
            return accessDeniedException.getMessage();
        }
        return "FORBIDDEN";
    }
}
