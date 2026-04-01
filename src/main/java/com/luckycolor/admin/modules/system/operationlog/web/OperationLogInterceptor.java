package com.luckycolor.admin.modules.system.operationlog.web;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import com.luckycolor.admin.modules.system.operationlog.mapper.OperationLogMapper;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogRecordCommand;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@ConditionalOnBean(OperationLogMapper.class)
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OperationLogInterceptor.class);

    private static final String START_TIME_ATTRIBUTE = OperationLogInterceptor.class.getName() + ".START_TIME";

    private static final int SUCCESS = 1;

    private static final int FAILURE = 0;

    private static final int MAX_TEXT_LENGTH = 1000;

    private static final Set<String> SUPPORTED_METHODS = Set.of("POST", "PUT", "DELETE");

    private final OperationLogService operationLogService;

    public OperationLogInterceptor(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (shouldRecord(request, handler)) {
            request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception exception
    ) {
        if (!shouldRecord(request, handler)) {
            return;
        }
        try {
            operationLogService.record(buildCommand(request, response, exception));
        } catch (RuntimeException recordException) {
            log.warn("failed to record operation log uri={}", request.getRequestURI(), recordException);
        }
    }

    private boolean shouldRecord(HttpServletRequest request, Object handler) {
        return handler instanceof HandlerMethod && SUPPORTED_METHODS.contains(request.getMethod());
    }

    private OperationLogRecordCommand buildCommand(
        HttpServletRequest request,
        HttpServletResponse response,
        Exception exception
    ) {
        long startTime = request.getAttribute(START_TIME_ATTRIBUTE) instanceof Long value
            ? value
            : System.currentTimeMillis();
        long durationMs = Math.max(System.currentTimeMillis() - startTime, 0L);
        JwtAuthenticatedUser principal = resolvePrincipal();
        String requestPath = resolveRequestPath(request);
        return new OperationLogRecordCommand(
            principal != null ? principal.tenantId() : TenantContextHolder.getOptionalTenantId().orElse(null),
            principal != null ? principal.userId() : null,
            principal != null ? principal.username() : null,
            resolveBizModule(requestPath),
            resolveOperationType(request.getMethod(), requestPath),
            request.getMethod(),
            truncate(requestPath),
            truncate(buildRequestParams(request)),
            resolveSuccess(response, exception),
            response.getStatus(),
            durationMs,
            truncate(request.getRemoteAddr()),
            truncate(resolveErrorMessage(response, exception))
        );
    }

    private JwtAuthenticatedUser resolvePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            return null;
        }
        return principal;
    }

    private String resolveRequestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private String resolveBizModule(String requestPath) {
        String normalizedPath = requestPath;
        if (normalizedPath.startsWith("/admin/")) {
            normalizedPath = normalizedPath.substring("/admin/".length());
        } else if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        int slashIndex = normalizedPath.indexOf('/');
        return slashIndex >= 0 ? normalizedPath.substring(0, slashIndex) : normalizedPath;
    }

    private String resolveOperationType(String requestMethod, String requestPath) {
        if ("POST".equalsIgnoreCase(requestMethod)) {
            return "CREATE";
        }
        if ("DELETE".equalsIgnoreCase(requestMethod)) {
            return "DELETE";
        }
        if (!"PUT".equalsIgnoreCase(requestMethod)) {
            return requestMethod;
        }
        if (requestPath.endsWith("/status")) {
            return "STATUS";
        }
        if (requestPath.endsWith("/publish")) {
            return "PUBLISH";
        }
        if (requestPath.endsWith("/authority")) {
            return "AUTHORIZE";
        }
        if (requestPath.endsWith("/reset-password")) {
            return "RESET_PASSWORD";
        }
        if (requestPath.endsWith("/assign-roles")) {
            return "ASSIGN_ROLE";
        }
        return "UPDATE";
    }

    private String buildRequestParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return null;
        }
        return parameterMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + resolveParameterValue(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("&"));
    }

    private String resolveParameterValue(String parameterName, String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        if (isSensitiveParameter(parameterName)) {
            return "******";
        }
        return Arrays.stream(values)
            .map(this::truncate)
            .collect(Collectors.joining(","));
    }

    private boolean isSensitiveParameter(String parameterName) {
        if (!StringUtils.hasText(parameterName)) {
            return false;
        }
        String normalizedName = parameterName.trim().toLowerCase();
        return normalizedName.contains("password")
            || normalizedName.contains("token")
            || normalizedName.contains("secret")
            || normalizedName.contains("captcha");
    }

    private Integer resolveSuccess(HttpServletResponse response, Exception exception) {
        return exception == null && response.getStatus() < 400 ? SUCCESS : FAILURE;
    }

    private String resolveErrorMessage(HttpServletResponse response, Exception exception) {
        if (exception != null && StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        if (response.getStatus() >= 400) {
            return "HTTP_" + response.getStatus();
        }
        return null;
    }

    private String truncate(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() > MAX_TEXT_LENGTH ? value.substring(0, MAX_TEXT_LENGTH) : value;
    }
}
