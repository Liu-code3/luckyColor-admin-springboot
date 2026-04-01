package com.luckycolor.admin.modules.system.dictionary.item.web.response;

public record DictionaryItemDetailResponse(
    Long id,
    Long tenantId,
    String typeCode,
    Long parentId,
    String itemLabel,
    String itemValue,
    String itemTag,
    Integer sort,
    Integer status,
    String remark
) {
}
