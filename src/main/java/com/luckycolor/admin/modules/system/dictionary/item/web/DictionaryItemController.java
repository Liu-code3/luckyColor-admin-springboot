package com.luckycolor.admin.modules.system.dictionary.item.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.item.service.DictionaryItemService;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemTreeQuery;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemTreeResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/admin/dictionary-items")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "Dictionary Items", description = "Dictionary item management APIs")
public class DictionaryItemController {

    private final DictionaryItemService dictionaryItemService;

    public DictionaryItemController(DictionaryItemService dictionaryItemService) {
        this.dictionaryItemService = dictionaryItemService;
    }

    @GetMapping("/tree")
    @RequirePermission("system:dictionary:query")
    @Operation(summary = "List dictionary item tree")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary item tree loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<List<DictionaryItemTreeResponse>> tree(@ParameterObject DictionaryItemTreeQuery query) {
        return ApiResponse.success(dictionaryItemService.listDictionaryItemTree(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:dictionary:query")
    @Operation(summary = "Get dictionary item detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary item detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Dictionary item not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Dictionary item not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<DictionaryItemDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(dictionaryItemService.getDictionaryItem(id));
    }

    @PostMapping
    @RequirePermission("system:dictionary:create")
    @Operation(summary = "Create dictionary item")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary item created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"itemLabel must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Long> create(@Valid @RequestBody DictionaryItemSaveRequest request) {
        return ApiResponse.success(dictionaryItemService.createDictionaryItem(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dictionary:update")
    @Operation(summary = "Update dictionary item")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary item updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"itemLabel must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
            responseCode = "404", description = "Dictionary item not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Dictionary item not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody DictionaryItemSaveRequest request) {
        dictionaryItemService.updateDictionaryItem(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dictionary:delete")
    @Operation(summary = "Delete dictionary item")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dictionary item deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Dictionary item not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Dictionary item not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        dictionaryItemService.deleteDictionaryItem(id);
        return ApiResponse.success(true);
    }
}
