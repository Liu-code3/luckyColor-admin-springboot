package com.luckycolor.admin.modules.platform.codegen.web.response;

import java.util.List;

public record CodegenTableDetailResponse(
    Long id,
    Long tenantId,
    String physicalTableName,
    String tableComment,
    String businessName,
    String className,
    String moduleName,
    String packageName,
    String genMode,
    Integer columnCount,
    String remark,
    List<CodegenColumnResponse> columns
) {
}
