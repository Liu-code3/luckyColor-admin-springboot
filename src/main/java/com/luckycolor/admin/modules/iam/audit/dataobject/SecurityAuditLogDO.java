package com.luckycolor.admin.modules.iam.audit.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_security_audit_log")
public class SecurityAuditLogDO extends BaseDO {

    private Long tenantId;

    private Long userId;

    private String username;

    private String eventType;

    private Integer success;

    private String requestMethod;

    private String requestUri;

    private String remoteIp;

    private String reason;
}
