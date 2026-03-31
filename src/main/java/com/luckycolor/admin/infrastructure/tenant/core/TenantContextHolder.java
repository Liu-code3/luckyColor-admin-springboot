package com.luckycolor.admin.infrastructure.tenant.core;

import java.util.Optional;

public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID_HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    public static Optional<Long> getOptionalTenantId() {
        return Optional.ofNullable(TENANT_ID_HOLDER.get());
    }

    public static long getRequiredTenantId() {
        Long tenantId = TENANT_ID_HOLDER.get();
        if (tenantId == null) {
            throw new IllegalStateException("Missing tenant id in current request context");
        }
        return tenantId;
    }

    public static void clear() {
        TENANT_ID_HOLDER.remove();
    }
}
