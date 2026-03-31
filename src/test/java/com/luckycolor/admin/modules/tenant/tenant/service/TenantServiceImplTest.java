package com.luckycolor.admin.modules.tenant.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.impl.TenantServiceImpl;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TenantServiceImplTest {

    @Test
    void shouldConvertTenantPageResult() {
        TenantMapper mapper = Mockito.mock(TenantMapper.class);
        TenantService service = new TenantServiceImpl(mapper);
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
}
