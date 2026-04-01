package com.luckycolor.admin.modules.platform.codegen.web.response;

public record CodegenColumnResponse(
    Long id,
    String columnName,
    String columnComment,
    String jdbcType,
    String javaType,
    String javaField,
    String htmlType,
    String queryType,
    Integer required,
    Integer listVisible,
    Integer formVisible,
    Integer status
) {
}
