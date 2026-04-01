package com.luckycolor.admin.modules.system.dictionary.type.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import com.luckycolor.admin.modules.system.dictionary.type.service.DictionaryTypeService;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypePageQuery;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypeSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypeDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypePageResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/admin/dictionary-types")
@ConditionalOnBean(DictionaryTypeMapper.class)
@Validated
public class DictionaryTypeController {

    private final DictionaryTypeService dictionaryTypeService;

    public DictionaryTypeController(DictionaryTypeService dictionaryTypeService) {
        this.dictionaryTypeService = dictionaryTypeService;
    }

    @GetMapping("/page")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<PageResult<DictionaryTypePageResponse>> page(DictionaryTypePageQuery query) {
        return ApiResponse.success(dictionaryTypeService.pageDictionaryTypes(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<DictionaryTypeDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(dictionaryTypeService.getDictionaryType(id));
    }

    @PostMapping
    @RequirePermission("system:dictionary:create")
    public ApiResponse<Long> create(@Valid @RequestBody DictionaryTypeSaveRequest request) {
        return ApiResponse.success(dictionaryTypeService.createDictionaryType(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dictionary:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody DictionaryTypeSaveRequest request) {
        dictionaryTypeService.updateDictionaryType(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dictionary:delete")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        dictionaryTypeService.deleteDictionaryType(id);
        return ApiResponse.success(true);
    }
}
