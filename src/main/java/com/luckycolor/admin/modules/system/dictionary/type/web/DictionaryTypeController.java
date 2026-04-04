package com.luckycolor.admin.modules.system.dictionary.type.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import com.luckycolor.admin.modules.system.dictionary.type.service.DictionaryTypeService;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypePageQuery;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypeSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypeDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypePageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
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
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "Dictionary Types", description = "Dictionary type management APIs")
public class DictionaryTypeController {

    private final DictionaryTypeService dictionaryTypeService;

    public DictionaryTypeController(DictionaryTypeService dictionaryTypeService) {
        this.dictionaryTypeService = dictionaryTypeService;
    }

    @GetMapping("/page")
    @RequirePermission("system:dictionary:query")
    @Operation(summary = "Page dictionary types")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary types loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<DictionaryTypePageResponse>> page(@ParameterObject DictionaryTypePageQuery query) {
        return ApiResponse.success(dictionaryTypeService.pageDictionaryTypes(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:dictionary:query")
    @Operation(summary = "Get dictionary type detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary type detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Dictionary type not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Dictionary type not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<DictionaryTypeDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(dictionaryTypeService.getDictionaryType(id));
    }

    @PostMapping
    @RequirePermission("system:dictionary:create")
    @Operation(summary = "Create dictionary type")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary type created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"typeCode must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Long> create(@Valid @RequestBody DictionaryTypeSaveRequest request) {
        return ApiResponse.success(dictionaryTypeService.createDictionaryType(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dictionary:update")
    @Operation(summary = "Update dictionary type")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary type updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"typeCode must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Dictionary type not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Dictionary type not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody DictionaryTypeSaveRequest request) {
        dictionaryTypeService.updateDictionaryType(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dictionary:delete")
    @Operation(summary = "Delete dictionary type")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary type deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Dictionary type not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Dictionary type not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        dictionaryTypeService.deleteDictionaryType(id);
        return ApiResponse.success(true);
    }
}
