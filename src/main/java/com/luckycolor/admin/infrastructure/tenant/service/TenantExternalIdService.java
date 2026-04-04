package com.luckycolor.admin.infrastructure.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.modules.tenant.profile.dataobject.TenantProfileDO;
import com.luckycolor.admin.modules.tenant.profile.mapper.TenantProfileMapper;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TenantExternalIdService {

    private final TenantProfileMapper tenantProfileMapper;

    public TenantExternalIdService(@Nullable TenantProfileMapper tenantProfileMapper) {
        this.tenantProfileMapper = tenantProfileMapper;
    }

    public Optional<Long> resolveTenantId(String externalTenantId) {
        if (!StringUtils.hasText(externalTenantId)) {
            return Optional.empty();
        }
        String normalized = externalTenantId.trim();
        try {
            return Optional.of(Long.parseLong(normalized));
        } catch (NumberFormatException ignored) {
            // Non-numeric tenant IDs are resolved from tenant profile codes.
        }
        if (normalized.matches("tenant_\\d+")) {
            return Optional.of(Long.parseLong(normalized.substring("tenant_".length())));
        }
        if (tenantProfileMapper == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<TenantProfileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantProfileDO::getTenantCode, normalized);
        TenantProfileDO profile = tenantProfileMapper.selectOne(queryWrapper);
        return profile == null ? Optional.empty() : Optional.ofNullable(profile.getId());
    }

    public String toExternalTenantId(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        if (tenantProfileMapper != null) {
            TenantProfileDO profile = tenantProfileMapper.selectById(tenantId);
            if (profile != null && StringUtils.hasText(profile.getTenantCode())) {
                return profile.getTenantCode().trim();
            }
        }
        return String.format("tenant_%03d", tenantId);
    }
}
