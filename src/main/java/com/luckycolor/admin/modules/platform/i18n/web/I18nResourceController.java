package com.luckycolor.admin.modules.platform.i18n.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.platform.i18n.mapper.I18nResourceMapper;
import com.luckycolor.admin.modules.platform.i18n.service.I18nResourceService;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourcePageQuery;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceSaveRequest;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceStatusRequest;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourceDetailResponse;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourcePageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/i18n-resources")
@Validated
@ConditionalOnPersistenceEnabled
@Tag(name = "I18n Resources", description = "Internationalization resource management APIs")
public class I18nResourceController {

    private final I18nResourceService i18nResourceService;

    public I18nResourceController(I18nResourceService i18nResourceService) {
        this.i18nResourceService = i18nResourceService;
    }

    @GetMapping("/page")
    @RequirePermission("i18n:query")
    @Operation(summary = "Page i18n resources")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "I18n resources loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<I18nResourcePageResponse>> page(@ParameterObject I18nResourcePageQuery query) {
        return ApiResponse.success(i18nResourceService.pageResources(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("i18n:query")
    @Operation(summary = "Get i18n resource detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "I18n resource detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "I18n resource not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"I18n resource not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<I18nResourceDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(i18nResourceService.getResource(id));
    }

    @PostMapping
    @RequirePermission("i18n:create")
    @Operation(summary = "Create i18n resource")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "I18n resource created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"resourceKey must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Long> create(@Valid @RequestBody I18nResourceSaveRequest request) {
        return ApiResponse.success(i18nResourceService.createResource(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("i18n:update")
    @Operation(summary = "Update i18n resource")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "I18n resource updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"resourceKey must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
            responseCode = "404", description = "I18n resource not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"I18n resource not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody I18nResourceSaveRequest request) {
        i18nResourceService.updateResource(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("i18n:update")
    @Operation(summary = "Update i18n resource status")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "I18n resource status updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"enabled must not be null\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
            responseCode = "404", description = "I18n resource not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"I18n resource not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody I18nResourceStatusRequest request) {
        i18nResourceService.updateStatus(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/version")
    @RequirePermission("i18n:update")
    @Operation(summary = "Increase i18n resource version")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "I18n resource version increased"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "I18n resource not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"I18n resource not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> bumpVersion(@PathVariable Long id) {
        i18nResourceService.bumpVersion(id);
        return ApiResponse.success(true);
    }
}
