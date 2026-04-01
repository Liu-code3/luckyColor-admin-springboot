package com.luckycolor.admin.modules.system.dictionary.catalog.web.response;

import java.util.List;

public record DictionaryCatalogResponse(
    String typeCode,
    String typeName,
    List<DictionaryCatalogItemResponse> items
) {
}
