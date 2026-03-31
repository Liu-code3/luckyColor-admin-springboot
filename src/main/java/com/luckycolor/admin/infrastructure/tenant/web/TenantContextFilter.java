package com.luckycolor.admin.infrastructure.tenant.web;

import com.luckycolor.admin.infrastructure.tenant.config.TenancyProperties;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
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
            tenantId = resolveTenantFromDomain(request.getServerName()).orElse(null);
        }
        if (!StringUtils.hasText(tenantId)) {
            tenantId = tenancyProperties.getDefaultTenantId();
        }
        if (!StringUtils.hasText(tenantId)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(tenantId.trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid tenant id: " + tenantId, exception);
        }
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
