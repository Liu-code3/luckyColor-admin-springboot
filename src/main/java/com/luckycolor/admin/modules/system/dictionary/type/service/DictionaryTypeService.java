package com.luckycolor.admin.modules.system.dictionary.type.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypePageQuery;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypeSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypeDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypePageResponse;

public interface DictionaryTypeService {

    PageResult<DictionaryTypePageResponse> pageDictionaryTypes(DictionaryTypePageQuery query);

    DictionaryTypeDetailResponse getDictionaryType(Long id);

    Long createDictionaryType(DictionaryTypeSaveRequest request);

    void updateDictionaryType(Long id, DictionaryTypeSaveRequest request);

    void deleteDictionaryType(Long id);
}
