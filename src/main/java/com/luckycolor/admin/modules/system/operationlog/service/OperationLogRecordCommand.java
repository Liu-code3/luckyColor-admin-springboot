package com.luckycolor.admin.modules.system.operationlog.service;

public record OperationLogRecordCommand(
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
    String errorMessage
) {
}
