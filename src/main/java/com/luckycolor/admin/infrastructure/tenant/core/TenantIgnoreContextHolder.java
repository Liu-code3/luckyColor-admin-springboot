package com.luckycolor.admin.infrastructure.tenant.core;

public final class TenantIgnoreContextHolder {

    private static final ThreadLocal<Integer> IGNORE_HOLDER = ThreadLocal.withInitial(() -> 0);

    private TenantIgnoreContextHolder() {
    }

    public static void enter() {
        IGNORE_HOLDER.set(IGNORE_HOLDER.get() + 1);
    }

    public static void exit() {
        int currentDepth = IGNORE_HOLDER.get() - 1;
        if (currentDepth <= 0) {
            IGNORE_HOLDER.remove();
            return;
        }
        IGNORE_HOLDER.set(currentDepth);
    }

    public static boolean isIgnoreTenant() {
        return IGNORE_HOLDER.get() > 0;
    }

    public static void clear() {
        IGNORE_HOLDER.remove();
    }
}
