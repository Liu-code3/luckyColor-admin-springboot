package com.luckycolor.admin.modules.system.dictionary.type.web.response;

public record DictionaryTypeDetailResponse(
    Long id,
    Long tenantId,
    String typeCode,
    String typeName,
    Integer status,
    Integer sort,
    String remark
) {
}
