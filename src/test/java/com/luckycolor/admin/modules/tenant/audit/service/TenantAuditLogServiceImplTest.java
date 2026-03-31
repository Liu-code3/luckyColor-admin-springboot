package com.luckycolor.admin.modules.tenant.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeRule;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeType;
import com.luckycolor.admin.modules.tenant.audit.dataobject.TenantAuditLogDO;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.audit.service.impl.TenantAuditLogServiceImpl;
import com.luckycolor.admin.modules.tenant.audit.web.request.TenantAuditLogPageQuery;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;
import com.luckycolor.admin.support.MyBatisTableInfoTestUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class TenantAuditLogServiceImplTest {

    @Test
    void shouldReturnAuditLogPage() {
        TenantAuditLogMapper mapper = Mockito.mock(TenantAuditLogMapper.class);
        TenantAuditLogService service = new TenantAuditLogServiceImpl(mapper, noScopeBuilder());
        TenantAuditLogDO auditLog = new TenantAuditLogDO();
        auditLog.setId(1L);
        auditLog.setTenantId(1001L);
        auditLog.setTargetType("TENANT");
        auditLog.setTargetId(1001L);
        auditLog.setAction("UPDATE");
        auditLog.setContent("Lucky Color");
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(auditLog), 1L));

        PageResult<TenantAuditLogPageResponse> result = service.pageTenantAuditLogs(new TenantAuditLogPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(TenantAuditLogPageResponse::action).containsExactly("UPDATE");
    }

    @Test
    void shouldAppendTenantDataScopeToAuditLogQuery() {
        MyBatisTableInfoTestUtils.initTableInfo(TenantAuditLogDO.class);
        TenantAuditLogMapper mapper = Mockito.mock(TenantAuditLogMapper.class);
        TenantAuditLogService service = new TenantAuditLogServiceImpl(mapper, tenantScopeBuilder(1001L));
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(), 0L));

        service.pageTenantAuditLogs(new TenantAuditLogPageQuery());

        ArgumentCaptor<Wrapper<TenantAuditLogDO>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        Mockito.verify(mapper).selectPageResult(any(), wrapperCaptor.capture());
        LambdaQueryWrapper<TenantAuditLogDO> wrapper = (LambdaQueryWrapper<TenantAuditLogDO>) wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("tenant_id");
        assertThat(wrapper.getParamNameValuePairs()).containsValue(1001L);
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }

    private DataScopeConditionBuilder tenantScopeBuilder(Long tenantId) {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(
            java.util.Optional.of(new DataScopeRule(DataScopeType.TENANT, tenantId, null, List.of(tenantId), List.of()))
        );
        return new DataScopeConditionBuilder(resolver);
    }
}
