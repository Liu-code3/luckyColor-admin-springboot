package com.luckycolor.admin.modules.tenant.audit.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantAuditLogPageQuery extends PageQuery {

    private Long tenantId;

    private String targetType;

    private String action;
}
