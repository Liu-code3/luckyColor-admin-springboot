package com.luckycolor.admin.modules.system.dictionary.catalog.web.response;

import java.util.List;

public record DictionaryCatalogItemResponse(
    Long id,
    Long parentId,
    String label,
    String value,
    String tag,
    List<DictionaryCatalogItemResponse> children
) {
}
