package com.luckycolor.admin.modules.tenant.packageinfo.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;

public interface TenantPackageService {

    PageResult<TenantPackagePageResponse> pageTenantPackages(TenantPackagePageQuery query);
}
