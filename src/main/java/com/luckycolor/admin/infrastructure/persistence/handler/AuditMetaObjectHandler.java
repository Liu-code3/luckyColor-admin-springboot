package com.luckycolor.admin.infrastructure.persistence.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String SYSTEM_OPERATOR = "system";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String operator = resolveOperator();

        fillIfAbsent(metaObject, "createTime", now);
        fillIfAbsent(metaObject, "updateTime", now);
        fillIfAbsent(metaObject, "createBy", operator);
        fillIfAbsent(metaObject, "updateBy", operator);

        TenantContextHolder.getOptionalTenantId()
            .ifPresent(tenantId -> fillIfAbsent(metaObject, "tenantId", tenantId));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        setFieldValByName("updateBy", resolveOperator(), metaObject);
    }

    private String resolveOperator() {
        return SYSTEM_OPERATOR;
    }

    private void fillIfAbsent(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasSetter(fieldName) && getFieldValByName(fieldName, metaObject) == null) {
            setFieldValByName(fieldName, value, metaObject);
        }
    }
}
