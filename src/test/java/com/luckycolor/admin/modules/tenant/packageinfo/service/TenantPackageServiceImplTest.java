package com.luckycolor.admin.modules.tenant.packageinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.packageinfo.dataobject.TenantPackageDO;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.service.impl.TenantPackageServiceImpl;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TenantPackageServiceImplTest {

    @Test
    void shouldConvertPageResult() {
        TenantPackageMapper mapper = Mockito.mock(TenantPackageMapper.class);
        TenantPackageService service = new TenantPackageServiceImpl(mapper);
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
}
