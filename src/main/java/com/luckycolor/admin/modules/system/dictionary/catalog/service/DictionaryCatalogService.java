package com.luckycolor.admin.modules.system.dictionary.catalog.service;

import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import java.util.List;

public interface DictionaryCatalogService {

    List<DictionaryCatalogItemResponse> listItemsByType(String typeCode);

    List<DictionaryCatalogResponse> listCatalog(List<String> typeCodes);
}
