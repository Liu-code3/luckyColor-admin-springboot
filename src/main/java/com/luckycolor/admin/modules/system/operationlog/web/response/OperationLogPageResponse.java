package com.luckycolor.admin.modules.system.operationlog.web.response;

import java.time.LocalDateTime;

public record OperationLogPageResponse(
    Long id,
    Long tenantId,
    Long userId,
    String username,
    String bizModule,
    String operationType,
    String requestMethod,
    String requestUri,
    String requestParams,
    Integer success,
    Integer statusCode,
    Long durationMs,
    String remoteIp,
    String errorMessage,
    LocalDateTime createTime
) {
}
