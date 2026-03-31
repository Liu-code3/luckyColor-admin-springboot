package com.luckycolor.admin.modules.tenant.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.dataobject.TenantAuditLogDO;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.audit.service.impl.TenantAuditLogServiceImpl;
import com.luckycolor.admin.modules.tenant.audit.web.request.TenantAuditLogPageQuery;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TenantAuditLogServiceImplTest {

    @Test
    void shouldReturnAuditLogPage() {
        TenantAuditLogMapper mapper = Mockito.mock(TenantAuditLogMapper.class);
        TenantAuditLogService service = new TenantAuditLogServiceImpl(mapper);
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
}
