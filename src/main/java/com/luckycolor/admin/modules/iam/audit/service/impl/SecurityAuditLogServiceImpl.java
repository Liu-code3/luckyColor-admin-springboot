package com.luckycolor.admin.modules.iam.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAccessTokenClaims;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.infrastructure.tenant.config.TenancyProperties;
import com.luckycolor.admin.modules.iam.audit.dataobject.SecurityAuditLogDO;
import com.luckycolor.admin.modules.iam.audit.mapper.SecurityAuditLogMapper;
import com.luckycolor.admin.modules.iam.audit.service.SecurityAuditLogService;
import com.luckycolor.admin.modules.iam.audit.web.request.SecurityAuditLogPageQuery;
import com.luckycolor.admin.modules.iam.audit.web.response.SecurityAuditLogPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@TenantIgnore
@ConditionalOnPersistenceEnabled
public class SecurityAuditLogServiceImpl implements SecurityAuditLogService {

    private static final int SUCCESS = 1;

    private static final int FAILURE = 0;

    private final SecurityAuditLogMapper securityAuditLogMapper;
    private final JwtTokenService jwtTokenService;
    private final TenancyProperties tenancyProperties;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public SecurityAuditLogServiceImpl(
        SecurityAuditLogMapper securityAuditLogMapper,
        JwtTokenService jwtTokenService,
        TenancyProperties tenancyProperties,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.securityAuditLogMapper = securityAuditLogMapper;
        this.jwtTokenService = jwtTokenService;
        this.tenancyProperties = tenancyProperties;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<SecurityAuditLogPageResponse> pageSecurityAuditLogs(SecurityAuditLogPageQuery query) {
        PageResult<SecurityAuditLogDO> pageResult = securityAuditLogMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public void recordLoginSuccess(Long userId, String username, Long tenantId, String remoteIp) {
        record(userId, username, tenantId, "LOGIN_SUCCESS", SUCCESS, null, null, remoteIp, null);
    }

    @Override
    public void recordLoginFailure(Long userId, String username, Long tenantId, String remoteIp, String reason) {
        record(userId, username, tenantId, "LOGIN_FAILURE", FAILURE, null, null, remoteIp, reason);
    }

    @Override
    public void recordLogout(Long userId, String username, Long tenantId, String remoteIp) {
        record(userId, username, tenantId, "LOGOUT", SUCCESS, null, null, remoteIp, null);
    }

    @Override
    public void recordUnauthorized(HttpServletRequest request, String reason) {
        RequestAuditContext context = resolveRequestContext(request, null);
        record(
            context.userId(),
            context.username(),
            context.tenantId(),
            "UNAUTHORIZED",
            FAILURE,
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            reason
        );
    }

    @Override
    public void recordAccessDenied(HttpServletRequest request, Authentication authentication, String reason) {
        RequestAuditContext context = resolveRequestContext(request, authentication);
        record(
            context.userId(),
            context.username(),
            context.tenantId(),
            "ACCESS_DENIED",
            FAILURE,
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            reason
        );
    }

    private LambdaQueryWrapper<SecurityAuditLogDO> buildQueryWrapper(SecurityAuditLogPageQuery query) {
        LambdaQueryWrapper<SecurityAuditLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(query.getTenantId() != null, SecurityAuditLogDO::getTenantId, query.getTenantId());
        queryWrapper.like(StringUtils.hasText(query.getUsername()), SecurityAuditLogDO::getUsername, query.getUsername());
        queryWrapper.eq(StringUtils.hasText(query.getEventType()), SecurityAuditLogDO::getEventType, query.getEventType());
        queryWrapper.eq(query.getSuccess() != null, SecurityAuditLogDO::getSuccess, query.getSuccess());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SecurityAuditLogDO::getTenantId, null);
        queryWrapper.orderByDesc(SecurityAuditLogDO::getCreateTime);
        return queryWrapper;
    }

    private void record(
        Long userId,
        String username,
        Long tenantId,
        String eventType,
        Integer success,
        String requestMethod,
        String requestUri,
        String remoteIp,
        String reason
    ) {
        SecurityAuditLogDO auditLog = new SecurityAuditLogDO();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setTenantId(tenantId);
        auditLog.setEventType(eventType);
        auditLog.setSuccess(success);
        auditLog.setRequestMethod(requestMethod);
        auditLog.setRequestUri(requestUri);
        auditLog.setRemoteIp(remoteIp);
        auditLog.setReason(reason);
        securityAuditLogMapper.insert(auditLog);
    }

    private RequestAuditContext resolveRequestContext(HttpServletRequest request, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof JwtAuthenticatedUser principal) {
            return new RequestAuditContext(principal.userId(), principal.username(), principal.tenantId());
        }
        Long headerTenantId = parseTenantId(request.getHeader(tenancyProperties.getHeader())).orElse(null);
        JwtAccessTokenClaims claims = resolveClaims(request.getHeader("Authorization")).orElse(null);
        if (claims == null) {
            return new RequestAuditContext(null, null, headerTenantId);
        }
        Long tenantId = claims.tenantId() != null ? claims.tenantId() : headerTenantId;
        return new RequestAuditContext(claims.userId(), claims.username(), tenantId);
    }

    private Optional<JwtAccessTokenClaims> resolveClaims(String authorizationHeader) {
        String token = jwtTokenService.resolveBearerToken(authorizationHeader);
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            return Optional.of(jwtTokenService.parseAccessToken(token));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Optional<Long> parseTenantId(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(tenantId.trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private SecurityAuditLogPageResponse toPageResponse(SecurityAuditLogDO auditLog) {
        return new SecurityAuditLogPageResponse(
            auditLog.getId(),
            auditLog.getTenantId(),
            auditLog.getUserId(),
            auditLog.getUsername(),
            auditLog.getEventType(),
            auditLog.getSuccess(),
            auditLog.getRequestMethod(),
            auditLog.getRequestUri(),
            auditLog.getRemoteIp(),
            auditLog.getReason(),
            auditLog.getCreateTime()
        );
    }

    private record RequestAuditContext(Long userId, String username, Long tenantId) {
    }
}
