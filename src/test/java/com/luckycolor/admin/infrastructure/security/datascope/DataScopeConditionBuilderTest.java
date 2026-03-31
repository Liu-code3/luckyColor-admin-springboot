package com.luckycolor.admin.infrastructure.security.datascope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.modules.tenant.audit.dataobject.TenantAuditLogDO;
import com.luckycolor.admin.support.MyBatisTableInfoTestUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataScopeConditionBuilderTest {

    @Test
    void shouldAppendTenantFilterForTenantScope() {
        MyBatisTableInfoTestUtils.initTableInfo(TenantAuditLogDO.class);
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(
            Optional.of(new DataScopeRule(DataScopeType.TENANT, 1001L, null, List.of(1001L), List.of()))
        );
        DataScopeConditionBuilder builder = new DataScopeConditionBuilder(resolver);
        LambdaQueryWrapper<TenantAuditLogDO> wrapper = new LambdaQueryWrapper<>();

        builder.applyCurrentScope(wrapper, TenantAuditLogDO::getTenantId, null);

        assertThat(wrapper.getSqlSegment()).contains("tenant_id");
        assertThat(wrapper.getParamNameValuePairs()).containsValue(1001L);
    }

    @Test
    void shouldAppendInFilterForCustomScope() {
        MyBatisTableInfoTestUtils.initTableInfo(TenantAuditLogDO.class);
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(
            Optional.of(new DataScopeRule(DataScopeType.CUSTOM, 1L, null, List.of(1L, 2L), List.of()))
        );
        DataScopeConditionBuilder builder = new DataScopeConditionBuilder(resolver);
        LambdaQueryWrapper<TenantAuditLogDO> wrapper = new LambdaQueryWrapper<>();

        builder.applyCurrentScope(wrapper, TenantAuditLogDO::getTenantId, null);

        assertThat(wrapper.getSqlSegment()).contains("tenant_id");
        assertThat(wrapper.getParamNameValuePairs()).containsValue(1L);
        assertThat(wrapper.getParamNameValuePairs()).containsValue(2L);
    }

    @Test
    void shouldDenyAllWhenCustomScopeHasNoSupportedColumns() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(
            Optional.of(new DataScopeRule(DataScopeType.CUSTOM, null, 100L, List.of(), List.of(100L, 101L)))
        );
        DataScopeConditionBuilder builder = new DataScopeConditionBuilder(resolver);
        LambdaQueryWrapper<TenantAuditLogDO> wrapper = new LambdaQueryWrapper<>();

        builder.applyCurrentScope(wrapper, null, null);

        assertThat(wrapper.getSqlSegment()).contains("1 = 0");
    }
}
