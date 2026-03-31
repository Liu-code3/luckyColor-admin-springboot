package com.luckycolor.admin.modules.tenant.tenant.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;

public interface TenantService {

    PageResult<TenantPageResponse> pageTenants(TenantPageQuery query);
}
