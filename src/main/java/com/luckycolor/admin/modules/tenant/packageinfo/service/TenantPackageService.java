package com.luckycolor.admin.modules.tenant.packageinfo.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageSaveRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageStatusRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackageDetailResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;

public interface TenantPackageService {

    PageResult<TenantPackagePageResponse> pageTenantPackages(TenantPackagePageQuery query);

    TenantPackageDetailResponse getTenantPackage(Long id);

    Long createTenantPackage(TenantPackageSaveRequest request);

    void updateTenantPackage(Long id, TenantPackageSaveRequest request);

    void updateTenantPackageStatus(Long id, TenantPackageStatusRequest request);
}
