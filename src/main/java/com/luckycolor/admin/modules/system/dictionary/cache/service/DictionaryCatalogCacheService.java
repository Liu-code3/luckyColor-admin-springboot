package com.luckycolor.admin.modules.system.dictionary.cache.service;

import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import java.util.List;

public interface DictionaryCatalogCacheService {

    List<DictionaryCatalogItemResponse> listItemsByType(String typeCode);

    List<DictionaryCatalogResponse> listCatalog(List<String> typeCodes);

    int refresh(List<String> typeCodes);

    void evict(List<String> typeCodes);
}
