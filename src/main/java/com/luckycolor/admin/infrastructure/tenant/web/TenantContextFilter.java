package com.luckycolor.admin.infrastructure.tenant.web;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAccessTokenClaims;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.infrastructure.security.web.SecurityRequestAttributes;
import com.luckycolor.admin.infrastructure.tenant.config.TenancyProperties;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import com.luckycolor.admin.infrastructure.tenant.service.TenantExternalIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final TenancyProperties tenancyProperties;
    private final JwtTokenService jwtTokenService;
    private final TenantExternalIdService tenantExternalIdService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !tenancyProperties.isEnabled()
            || tenancyProperties.isIgnoredPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            Optional<Long> tenantId = resolveTenantId(request);
            if (tenantId.isEmpty()) {
                if (request.getAttribute(SecurityRequestAttributes.AUTH_FAILURE_REASON) != null) {
                    filterChain.doFilter(request, response);
                    return;
                }
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tenant id");
                return;
            }

            TenantContextHolder.setTenantId(tenantId.get());
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private Optional<Long> resolveTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(tenancyProperties.getHeader());
        if (!StringUtils.hasText(tenantId)) {
            tenantId = resolveTenantFromToken(request).orElse(null);
        }
        if (!StringUtils.hasText(tenantId)) {
            tenantId = resolveTenantFromDomain(request.getServerName()).orElse(null);
        }
        if (!StringUtils.hasText(tenantId)) {
            tenantId = tenancyProperties.getDefaultTenantId();
        }
        if (!StringUtils.hasText(tenantId)) {
            return Optional.empty();
        }
        Optional<Long> resolvedTenantId = tenantExternalIdService.resolveTenantId(tenantId);
        if (resolvedTenantId.isPresent()) {
            return resolvedTenantId;
        }
        throw new IllegalArgumentException("Invalid tenant id: " + tenantId);
    }

    private Optional<String> resolveTenantFromToken(HttpServletRequest request) {
        String token = jwtTokenService.resolveBearerToken(request.getHeader("Authorization"));
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }

        JwtAccessTokenClaims claims;
        try {
            claims = jwtTokenService.parseAccessToken(token);
        } catch (RuntimeException exception) {
            request.setAttribute(SecurityRequestAttributes.AUTH_FAILURE_REASON, "TOKEN_INVALID");
            return Optional.empty();
        }
        if (claims.tenantId() == null) {
            return Optional.empty();
        }
        return Optional.of(String.valueOf(claims.tenantId()));
    }

    private Optional<String> resolveTenantFromDomain(String serverName) {
        String domainSuffix = tenancyProperties.getDomainSuffix();
        if (!StringUtils.hasText(domainSuffix) || !StringUtils.hasText(serverName)) {
            return Optional.empty();
        }

        String normalizedServerName = serverName.trim().toLowerCase();
        String normalizedDomainSuffix = domainSuffix.trim().toLowerCase();
        if (!normalizedServerName.endsWith(normalizedDomainSuffix)) {
            return Optional.empty();
        }

        String tenant = normalizedServerName.substring(
            0,
            normalizedServerName.length() - normalizedDomainSuffix.length()
        );
        if (!StringUtils.hasText(tenant)) {
            return Optional.empty();
        }

        if (tenant.endsWith(".")) {
            tenant = tenant.substring(0, tenant.length() - 1);
        }
        return StringUtils.hasText(tenant) ? Optional.of(tenant) : Optional.empty();
    }
}
