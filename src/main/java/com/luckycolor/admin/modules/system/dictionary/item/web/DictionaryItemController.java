package com.luckycolor.admin.modules.system.dictionary.item.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.item.service.DictionaryItemService;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemTreeQuery;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemTreeResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dictionary-items")
@ConditionalOnBean(DictionaryItemMapper.class)
@Validated
public class DictionaryItemController {

    private final DictionaryItemService dictionaryItemService;

    public DictionaryItemController(DictionaryItemService dictionaryItemService) {
        this.dictionaryItemService = dictionaryItemService;
    }

    @GetMapping("/tree")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<List<DictionaryItemTreeResponse>> tree(DictionaryItemTreeQuery query) {
        return ApiResponse.success(dictionaryItemService.listDictionaryItemTree(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<DictionaryItemDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(dictionaryItemService.getDictionaryItem(id));
    }

    @PostMapping
    @RequirePermission("system:dictionary:create")
    public ApiResponse<Long> create(@Valid @RequestBody DictionaryItemSaveRequest request) {
        return ApiResponse.success(dictionaryItemService.createDictionaryItem(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dictionary:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody DictionaryItemSaveRequest request) {
        dictionaryItemService.updateDictionaryItem(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dictionary:delete")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        dictionaryItemService.deleteDictionaryItem(id);
        return ApiResponse.success(true);
    }
}
