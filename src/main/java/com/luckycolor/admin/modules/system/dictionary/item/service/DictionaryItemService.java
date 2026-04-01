package com.luckycolor.admin.modules.system.dictionary.item.service;

import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemTreeQuery;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemTreeResponse;
import java.util.List;

public interface DictionaryItemService {

    List<DictionaryItemTreeResponse> listDictionaryItemTree(DictionaryItemTreeQuery query);

    DictionaryItemDetailResponse getDictionaryItem(Long id);

    Long createDictionaryItem(DictionaryItemSaveRequest request);

    void updateDictionaryItem(Long id, DictionaryItemSaveRequest request);

    void deleteDictionaryItem(Long id);
}
