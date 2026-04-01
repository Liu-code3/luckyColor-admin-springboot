package com.luckycolor.admin.modules.system.dictionary.catalog.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dictionaries")
@ConditionalOnBean(DictionaryCatalogCacheService.class)
@Validated
public class DictionaryCatalogController {

    private final DictionaryCatalogCacheService dictionaryCatalogCacheService;

    public DictionaryCatalogController(DictionaryCatalogCacheService dictionaryCatalogCacheService) {
        this.dictionaryCatalogCacheService = dictionaryCatalogCacheService;
    }

    @GetMapping("/{typeCode}/items")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<List<DictionaryCatalogItemResponse>> items(@PathVariable String typeCode) {
        return ApiResponse.success(dictionaryCatalogCacheService.listItemsByType(typeCode));
    }

    @GetMapping("/catalog")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<List<DictionaryCatalogResponse>> catalog(@RequestParam List<String> typeCodes) {
        return ApiResponse.success(dictionaryCatalogCacheService.listCatalog(typeCodes));
    }

    @PostMapping("/cache/refresh")
    @RequirePermission("system:dictionary:refresh-cache")
    public ApiResponse<Integer> refresh(@RequestParam(required = false) List<String> typeCodes) {
        return ApiResponse.success(dictionaryCatalogCacheService.refresh(typeCodes));
    }
}
