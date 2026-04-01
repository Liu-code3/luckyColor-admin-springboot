package com.luckycolor.admin.modules.system.operationlog.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_operation_log")
public class OperationLogDO extends TenantBaseDO {

    private Long userId;

    private String username;

    private String bizModule;

    private String operationType;

    private String requestMethod;

    private String requestUri;

    private String requestParams;

    private Integer success;

    private Integer statusCode;

    private Long durationMs;

    private String remoteIp;

    private String errorMessage;
}
