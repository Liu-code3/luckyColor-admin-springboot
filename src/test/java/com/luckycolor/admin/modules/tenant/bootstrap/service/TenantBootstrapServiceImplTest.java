package com.luckycolor.admin.modules.tenant.bootstrap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.bootstrap.config.TenantBootstrapProperties;
import com.luckycolor.admin.modules.tenant.bootstrap.dataobject.TenantBootstrapRecordDO;
import com.luckycolor.admin.modules.tenant.bootstrap.mapper.TenantBootstrapRecordMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.service.impl.TenantBootstrapServiceImpl;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapExecuteRequest;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapRecordPageQuery;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class TenantBootstrapServiceImplTest {

    @Test
    void shouldListTemplates() {
        TenantBootstrapService service = new TenantBootstrapServiceImpl(
            Mockito.mock(TenantMapper.class),
            Mockito.mock(TenantBootstrapRecordMapper.class),
            Mockito.mock(TenantAuditLogService.class),
            buildProperties()
        );

        List<TenantBootstrapTemplateResponse> result = service.listTemplates();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("default");
        assertThat(result.get(0).roleCodes()).containsExactly("tenant_admin", "tenant_member");
    }

    @Test
    void shouldCreateBootstrapRecord() {
        TenantMapper tenantMapper = Mockito.mock(TenantMapper.class);
        TenantBootstrapRecordMapper recordMapper = Mockito.mock(TenantBootstrapRecordMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantDO tenant = new TenantDO();
        tenant.setId(1L);
        tenant.setName("Lucky Color");
        when(tenantMapper.selectById(1L)).thenReturn(tenant);
        when(recordMapper.selectCount(any())).thenReturn(0L);
        when(recordMapper.insert(any(TenantBootstrapRecordDO.class))).thenAnswer(invocation -> {
            TenantBootstrapRecordDO record = invocation.getArgument(0);
            record.setId(2L);
            return 1;
        });
        TenantBootstrapService service = new TenantBootstrapServiceImpl(
            tenantMapper,
            recordMapper,
            auditLogService,
            buildProperties()
        );
        TenantBootstrapExecuteRequest request = new TenantBootstrapExecuteRequest();
        request.setTemplateCode("default");
        request.setAdminUsername("lucy-admin");

        TenantBootstrapRecordResponse result = service.bootstrapTenant(1L, request);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.adminUsername()).isEqualTo("lucy-admin");
        assertThat(result.menuCodes()).containsExactly("dashboard", "system:user", "system:role");
    }

    @Test
    void shouldRejectDuplicateBootstrap() {
        TenantMapper tenantMapper = Mockito.mock(TenantMapper.class);
        TenantBootstrapRecordMapper recordMapper = Mockito.mock(TenantBootstrapRecordMapper.class);
        TenantDO tenant = new TenantDO();
        tenant.setId(1L);
        tenant.setName("Lucky Color");
        when(tenantMapper.selectById(1L)).thenReturn(tenant);
        when(recordMapper.selectCount(any())).thenReturn(1L);
        TenantBootstrapService service = new TenantBootstrapServiceImpl(
            tenantMapper,
            recordMapper,
            Mockito.mock(TenantAuditLogService.class),
            buildProperties()
        );
        TenantBootstrapExecuteRequest request = new TenantBootstrapExecuteRequest();
        request.setTemplateCode("default");

        assertThatThrownBy(() -> service.bootstrapTenant(1L, request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void shouldConvertBootstrapRecordPage() {
        TenantMapper tenantMapper = Mockito.mock(TenantMapper.class);
        TenantBootstrapRecordMapper recordMapper = Mockito.mock(TenantBootstrapRecordMapper.class);
        TenantBootstrapRecordDO record = new TenantBootstrapRecordDO();
        record.setId(2L);
        record.setTenantId(1L);
        record.setTemplateCode("default");
        record.setTemplateName("默认租户模板");
        record.setRoleCodes("tenant_admin,tenant_member");
        record.setMenuCodes("dashboard,system:user");
        record.setAdminUsername("admin");
        record.setAdminNickname("租户管理员");
        record.setStatus(1);
        record.setBootstrapTime(LocalDateTime.of(2026, 3, 31, 12, 0));
        when(recordMapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(record), 1L));
        TenantBootstrapService service = new TenantBootstrapServiceImpl(
            tenantMapper,
            recordMapper,
            Mockito.mock(TenantAuditLogService.class),
            buildProperties()
        );

        PageResult<TenantBootstrapRecordResponse> result = service.pageRecords(new TenantBootstrapRecordPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList().get(0).templateCode()).isEqualTo("default");
        assertThat(result.getList().get(0).roleCodes()).containsExactly("tenant_admin", "tenant_member");
    }

    private TenantBootstrapProperties buildProperties() {
        TenantBootstrapProperties properties = new TenantBootstrapProperties();
        TenantBootstrapProperties.Template template = new TenantBootstrapProperties.Template();
        template.setCode("default");
        template.setName("默认租户模板");
        template.setRoleCodes(List.of("tenant_admin", "tenant_member"));
        template.setMenuCodes(List.of("dashboard", "system:user", "system:role"));
        template.setAdminUsername("admin");
        template.setAdminNickname("租户管理员");
        template.setStatus(0);
        template.setRemark("内置初始化模板");
        properties.setTemplates(List.of(template));
        return properties;
    }
}
