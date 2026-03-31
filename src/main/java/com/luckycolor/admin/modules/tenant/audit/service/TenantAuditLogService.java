package com.luckycolor.admin.modules.tenant.audit.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.tenant.audit.web.request.TenantAuditLogPageQuery;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;

public interface TenantAuditLogService {

    PageResult<TenantAuditLogPageResponse> pageTenantAuditLogs(TenantAuditLogPageQuery query);

    void record(Long tenantId, String targetType, Long targetId, String action, String content);
}
