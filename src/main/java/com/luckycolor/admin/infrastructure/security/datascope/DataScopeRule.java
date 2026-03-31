package com.luckycolor.admin.infrastructure.security.datascope;

import java.util.List;

public record DataScopeRule(
    DataScopeType type,
    Long tenantId,
    Long departmentId,
    List<Long> tenantIds,
    List<Long> departmentIds
) {
}
