package com.luckycolor.admin.infrastructure.security.authorization;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionGuardAspect {

    private final PermissionGuard permissionGuard;

    public PermissionGuardAspect(PermissionGuard permissionGuard) {
        this.permissionGuard = permissionGuard;
    }

    @Around(
        "@annotation(com.luckycolor.admin.infrastructure.security.authorization.RequirePermission)"
            + " || @within(com.luckycolor.admin.infrastructure.security.authorization.RequirePermission)"
            + " || @annotation(com.luckycolor.admin.infrastructure.security.authorization.RequireAnyPermission)"
            + " || @within(com.luckycolor.admin.infrastructure.security.authorization.RequireAnyPermission)"
    )
    public Object guard(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        RequirePermission requirePermission = resolveAnnotation(method, targetClass, RequirePermission.class);
        if (requirePermission != null) {
            permissionGuard.checkPermission(requirePermission.value());
        }

        RequireAnyPermission requireAnyPermission = resolveAnnotation(method, targetClass, RequireAnyPermission.class);
        if (requireAnyPermission != null) {
            permissionGuard.checkAnyPermission(requireAnyPermission.value());
        }

        return joinPoint.proceed();
    }

    private <A extends java.lang.annotation.Annotation> A resolveAnnotation(
        Method method,
        Class<?> targetClass,
        Class<A> annotationType
    ) {
        A methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, annotationType);
    }
}
