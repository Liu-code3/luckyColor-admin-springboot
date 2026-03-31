package com.luckycolor.admin.modules.tenant.tenant.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantExpireTimeRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantSaveRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantStatusRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantDetailResponse;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;

public interface TenantService {

    PageResult<TenantPageResponse> pageTenants(TenantPageQuery query);

    TenantDetailResponse getTenant(Long id);

    Long createTenant(TenantSaveRequest request);

    void updateTenant(Long id, TenantSaveRequest request);

    void updateTenantStatus(Long id, TenantStatusRequest request);

    void updateTenantExpireTime(Long id, TenantExpireTimeRequest request);
}
