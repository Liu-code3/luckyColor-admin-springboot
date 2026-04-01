package com.luckycolor.admin.modules.system.dictionary.catalog.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.catalog.service.DictionaryCatalogService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dictionaries")
@Validated
public class DictionaryCatalogController {

    private final DictionaryCatalogService dictionaryCatalogService;

    public DictionaryCatalogController(DictionaryCatalogService dictionaryCatalogService) {
        this.dictionaryCatalogService = dictionaryCatalogService;
    }

    @GetMapping("/{typeCode}/items")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<List<DictionaryCatalogItemResponse>> items(@PathVariable String typeCode) {
        return ApiResponse.success(dictionaryCatalogService.listItemsByType(typeCode));
    }

    @GetMapping("/catalog")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<List<DictionaryCatalogResponse>> catalog(@RequestParam List<String> typeCodes) {
        return ApiResponse.success(dictionaryCatalogService.listCatalog(typeCodes));
    }
}
