package com.luckycolor.admin.modules.system.dictionary.catalog.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dictionaries")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "Dictionary Catalog", description = "Dictionary catalog and cache APIs")
public class DictionaryCatalogController {

    private final DictionaryCatalogCacheService dictionaryCatalogCacheService;

    public DictionaryCatalogController(DictionaryCatalogCacheService dictionaryCatalogCacheService) {
        this.dictionaryCatalogCacheService = dictionaryCatalogCacheService;
    }

    @GetMapping("/{typeCode}/items")
    @RequirePermission("system:dictionary:query")
    @Operation(summary = "List items by dictionary type")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary items loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<List<DictionaryCatalogItemResponse>> items(@PathVariable String typeCode) {
        return ApiResponse.success(dictionaryCatalogCacheService.listItemsByType(typeCode));
    }

    @GetMapping("/catalog")
    @RequirePermission("system:dictionary:query")
    @Operation(summary = "Batch query dictionary catalog")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary catalog loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<List<DictionaryCatalogResponse>> catalog(@RequestParam List<String> typeCodes) {
        return ApiResponse.success(dictionaryCatalogCacheService.listCatalog(typeCodes));
    }

    @PostMapping("/cache/refresh")
    @RequirePermission("system:dictionary:refresh-cache")
    @Operation(summary = "Refresh dictionary cache")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary cache refreshed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Integer> refresh(@RequestParam(required = false) List<String> typeCodes) {
        return ApiResponse.success(dictionaryCatalogCacheService.refresh(typeCodes));
    }
}
