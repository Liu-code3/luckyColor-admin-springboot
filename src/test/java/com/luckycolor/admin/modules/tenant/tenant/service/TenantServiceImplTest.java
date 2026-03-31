package com.luckycolor.admin.modules.tenant.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.impl.TenantServiceImpl;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantExpireTimeRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantSaveRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantStatusRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantDetailResponse;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TenantServiceImplTest {

    @Test
    void shouldConvertTenantPageResult() {
        TenantMapper mapper = Mockito.mock(TenantMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantService service = new TenantServiceImpl(mapper, auditLogService);
        TenantDO tenant = new TenantDO();
        tenant.setId(1L);
        tenant.setName("Lucky Color");
        tenant.setPackageId(10L);
        tenant.setContactName("Liu");
        tenant.setContactMobile("13800000000");
        tenant.setAccountCount(50);
        tenant.setExpireTime(LocalDateTime.of(2026, 12, 31, 23, 59));
        tenant.setStatus(0);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(tenant), 1L));

        PageResult<TenantPageResponse> result = service.pageTenants(new TenantPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(TenantPageResponse::name).containsExactly("Lucky Color");
    }

    @Test
    void shouldReturnTenantDetail() {
        TenantMapper mapper = Mockito.mock(TenantMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantService service = new TenantServiceImpl(mapper, auditLogService);
        TenantDO tenant = buildTenant();
        when(mapper.selectById(1L)).thenReturn(tenant);

        TenantDetailResponse result = service.getTenant(1L);

        assertThat(result.name()).isEqualTo("Lucky Color");
    }

    @Test
    void shouldCreateTenant() {
        TenantMapper mapper = Mockito.mock(TenantMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantService service = new TenantServiceImpl(mapper, auditLogService);
        TenantSaveRequest request = buildSaveRequest();
        when(mapper.insert(any(TenantDO.class))).thenAnswer(invocation -> {
            TenantDO tenant = invocation.getArgument(0);
            tenant.setId(2L);
            return 1;
        });

        Long id = service.createTenant(request);

        assertThat(id).isEqualTo(2L);
    }

    @Test
    void shouldUpdateTenantStatus() {
        TenantMapper mapper = Mockito.mock(TenantMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantService service = new TenantServiceImpl(mapper, auditLogService);
        TenantDO tenant = buildTenant();
        when(mapper.selectById(1L)).thenReturn(tenant);
        when(mapper.updateById(any(TenantDO.class))).thenReturn(1);
        TenantStatusRequest request = new TenantStatusRequest();
        request.setStatus(1);

        service.updateTenantStatus(1L, request);

        assertThat(tenant.getStatus()).isEqualTo(1);
        Mockito.verify(mapper).selectById(eq(1L));
        Mockito.verify(mapper).updateById(eq(tenant));
    }

    @Test
    void shouldUpdateTenantExpireTime() {
        TenantMapper mapper = Mockito.mock(TenantMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantService service = new TenantServiceImpl(mapper, auditLogService);
        TenantDO tenant = buildTenant();
        when(mapper.selectById(1L)).thenReturn(tenant);
        when(mapper.updateById(any(TenantDO.class))).thenReturn(1);
        TenantExpireTimeRequest request = new TenantExpireTimeRequest();
        request.setExpireTime(LocalDateTime.of(2027, 1, 1, 0, 0));

        service.updateTenantExpireTime(1L, request);

        assertThat(tenant.getExpireTime()).isEqualTo(LocalDateTime.of(2027, 1, 1, 0, 0));
    }

    private TenantDO buildTenant() {
        TenantDO tenant = new TenantDO();
        tenant.setId(1L);
        tenant.setName("Lucky Color");
        tenant.setPackageId(10L);
        tenant.setContactName("Liu");
        tenant.setContactMobile("13800000000");
        tenant.setAccountCount(50);
        tenant.setExpireTime(LocalDateTime.of(2026, 12, 31, 23, 59));
        tenant.setStatus(0);
        return tenant;
    }

    private TenantSaveRequest buildSaveRequest() {
        TenantSaveRequest request = new TenantSaveRequest();
        request.setName("Lucky Color");
        request.setPackageId(10L);
        request.setContactName("Liu");
        request.setContactMobile("13800000000");
        request.setAccountCount(50);
        request.setExpireTime(LocalDateTime.of(2026, 12, 31, 23, 59));
        request.setStatus(0);
        return request;
    }
}
