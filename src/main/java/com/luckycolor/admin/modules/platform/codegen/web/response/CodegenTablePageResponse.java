package com.luckycolor.admin.modules.platform.codegen.web.response;

public record CodegenTablePageResponse(
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
    String remark
) {
}
