package com.luckycolor.admin.common.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    prefix = "app.persistence",
    name = "mapper-scan-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public @interface ConditionalOnPersistenceEnabled {
}
