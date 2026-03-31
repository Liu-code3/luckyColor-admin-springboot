package com.luckycolor.admin.infrastructure.tenant.aop;

import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.infrastructure.tenant.core.TenantIgnoreContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantIgnoreAspect {

    @Around("@within(com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore) "
        + "|| @annotation(com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        TenantIgnoreContextHolder.enter();
        try {
            return joinPoint.proceed();
        } finally {
            TenantIgnoreContextHolder.exit();
        }
    }
}
