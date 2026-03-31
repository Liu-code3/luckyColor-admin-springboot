package com.luckycolor.admin.modules.tenant.packageinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.packageinfo.dataobject.TenantPackageDO;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.service.impl.TenantPackageServiceImpl;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageSaveRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageStatusRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackageDetailResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TenantPackageServiceImplTest {

    @Test
    void shouldConvertPageResult() {
        TenantPackageMapper mapper = Mockito.mock(TenantPackageMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantPackageService service = new TenantPackageServiceImpl(mapper, auditLogService);
        TenantPackageDO tenantPackage = new TenantPackageDO();
        tenantPackage.setId(1L);
        tenantPackage.setPackageName("标准版");
        tenantPackage.setStatus(0);
        tenantPackage.setSort(10);
        tenantPackage.setRemark("default");
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(tenantPackage), 1L));

        PageResult<TenantPackagePageResponse> result = service.pageTenantPackages(new TenantPackagePageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(TenantPackagePageResponse::packageName).containsExactly("标准版");
    }

    @Test
    void shouldReturnDetail() {
        TenantPackageMapper mapper = Mockito.mock(TenantPackageMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantPackageService service = new TenantPackageServiceImpl(mapper, auditLogService);
        TenantPackageDO tenantPackage = new TenantPackageDO();
        tenantPackage.setId(1L);
        tenantPackage.setPackageName("标准版");
        tenantPackage.setStatus(0);
        tenantPackage.setSort(10);
        tenantPackage.setRemark("default");
        when(mapper.selectById(1L)).thenReturn(tenantPackage);

        TenantPackageDetailResponse result = service.getTenantPackage(1L);

        assertThat(result.packageName()).isEqualTo("标准版");
    }

    @Test
    void shouldCreateTenantPackage() {
        TenantPackageMapper mapper = Mockito.mock(TenantPackageMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantPackageService service = new TenantPackageServiceImpl(mapper, auditLogService);
        TenantPackageSaveRequest request = new TenantPackageSaveRequest();
        request.setPackageName("专业版");
        request.setStatus(0);
        request.setSort(20);
        request.setRemark("pro");
        when(mapper.insert(any(TenantPackageDO.class))).thenAnswer(invocation -> {
            TenantPackageDO dataObject = invocation.getArgument(0);
            dataObject.setId(2L);
            return 1;
        });

        Long id = service.createTenantPackage(request);

        assertThat(id).isEqualTo(2L);
    }

    @Test
    void shouldUpdateTenantPackageStatus() {
        TenantPackageMapper mapper = Mockito.mock(TenantPackageMapper.class);
        TenantAuditLogService auditLogService = Mockito.mock(TenantAuditLogService.class);
        TenantPackageService service = new TenantPackageServiceImpl(mapper, auditLogService);
        TenantPackageDO tenantPackage = new TenantPackageDO();
        tenantPackage.setId(1L);
        tenantPackage.setStatus(0);
        when(mapper.selectById(1L)).thenReturn(tenantPackage);
        when(mapper.updateById(any(TenantPackageDO.class))).thenReturn(1);
        TenantPackageStatusRequest request = new TenantPackageStatusRequest();
        request.setStatus(1);

        service.updateTenantPackageStatus(1L, request);

        assertThat(tenantPackage.getStatus()).isEqualTo(1);
        Mockito.verify(mapper).selectById(eq(1L));
        Mockito.verify(mapper).updateById(eq(tenantPackage));
    }
}
