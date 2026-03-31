package com.luckycolor.admin.modules.iam.audit.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityAuditLogPageQuery extends PageQuery {

    private Long tenantId;

    private String username;

    private String eventType;

    private Integer success;
}
