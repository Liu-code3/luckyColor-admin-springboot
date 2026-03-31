package com.luckycolor.admin.infrastructure.persistence.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuditMetaObjectHandlerTest {

    private final AuditMetaObjectHandler handler = new AuditMetaObjectHandler();

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldFillInsertFieldsAndTenantId() {
        TenantContextHolder.setTenantId(1001L);
        DemoTenantDO dataObject = new DemoTenantDO();

        handler.insertFill(SystemMetaObject.forObject(dataObject));

        assertThat(dataObject.getTenantId()).isEqualTo(1001L);
        assertThat(dataObject.getCreateBy()).isEqualTo("system");
        assertThat(dataObject.getUpdateBy()).isEqualTo("system");
        assertThat(dataObject.getCreateTime()).isNotNull();
        assertThat(dataObject.getUpdateTime()).isNotNull();
    }

    @Test
    void shouldFillUpdateFields() {
        DemoTenantDO dataObject = new DemoTenantDO();

        handler.updateFill(SystemMetaObject.forObject(dataObject));

        assertThat(dataObject.getUpdateBy()).isEqualTo("system");
        assertThat(dataObject.getUpdateTime()).isNotNull();
    }

    private static final class DemoTenantDO extends TenantBaseDO {
    }
}
