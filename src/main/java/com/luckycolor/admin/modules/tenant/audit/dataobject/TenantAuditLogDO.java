package com.luckycolor.admin.modules.tenant.audit.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_tenant_audit_log")
public class TenantAuditLogDO extends BaseDO {

    private Long tenantId;

    private String targetType;

    private Long targetId;

    private String action;

    private String content;
}
