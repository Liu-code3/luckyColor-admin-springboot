package com.luckycolor.admin.modules.tenant.bootstrap.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapExecuteRequest;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapRecordPageQuery;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import java.util.List;

public interface TenantBootstrapService {

    List<TenantBootstrapTemplateResponse> listTemplates();

    PageResult<TenantBootstrapRecordResponse> pageRecords(TenantBootstrapRecordPageQuery query);

    TenantBootstrapRecordResponse bootstrapTenant(Long tenantId, TenantBootstrapExecuteRequest request);
}
