package com.luckycolor.admin.modules.system.dictionary.item.web.response;

import java.util.List;

public record DictionaryItemTreeResponse(
    Long id,
    Long tenantId,
    String typeCode,
    Long parentId,
    String itemLabel,
    String itemValue,
    String itemTag,
    Integer sort,
    Integer status,
    List<DictionaryItemTreeResponse> children
) {
}
