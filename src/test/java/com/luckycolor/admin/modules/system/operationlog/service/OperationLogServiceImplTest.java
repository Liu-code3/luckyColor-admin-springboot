package com.luckycolor.admin.modules.system.operationlog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.operationlog.dataobject.OperationLogDO;
import com.luckycolor.admin.modules.system.operationlog.mapper.OperationLogMapper;
import com.luckycolor.admin.modules.system.operationlog.service.impl.OperationLogServiceImpl;
import com.luckycolor.admin.modules.system.operationlog.web.request.OperationLogPageQuery;
import com.luckycolor.admin.modules.system.operationlog.web.response.OperationLogPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OperationLogServiceImplTest {

    @Test
    void shouldReturnOperationLogPage() {
        OperationLogMapper mapper = Mockito.mock(OperationLogMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(operationLog()), 1L));
        OperationLogService service = new OperationLogServiceImpl(mapper, noScopeBuilder());

        PageResult<OperationLogPageResponse> result = service.pageOperationLogs(new OperationLogPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(OperationLogPageResponse::bizModule).containsExactly("users");
    }

    @Test
    void shouldRecordOperationLog() {
        OperationLogMapper mapper = Mockito.mock(OperationLogMapper.class);
        OperationLogService service = new OperationLogServiceImpl(mapper, noScopeBuilder());

        service.record(new OperationLogRecordCommand(
            1L,
            1L,
            "admin",
            "users",
            "CREATE",
            "POST",
            "/admin/users",
            "username=admin",
            1,
            200,
            12L,
            "127.0.0.1",
            null
        ));

        verify(mapper).insert(any(OperationLogDO.class));
    }

    private OperationLogDO operationLog() {
        OperationLogDO operationLog = new OperationLogDO();
        operationLog.setId(1L);
        operationLog.setTenantId(1L);
        operationLog.setUserId(1L);
        operationLog.setUsername("admin");
        operationLog.setBizModule("users");
        operationLog.setOperationType("CREATE");
        operationLog.setRequestMethod("POST");
        operationLog.setRequestUri("/admin/users");
        operationLog.setRequestParams("username=admin");
        operationLog.setSuccess(1);
        operationLog.setStatusCode(200);
        operationLog.setDurationMs(12L);
        operationLog.setRemoteIp("127.0.0.1");
        operationLog.setErrorMessage(null);
        operationLog.setCreateTime(LocalDateTime.of(2026, 4, 1, 12, 0));
        return operationLog;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }
}
