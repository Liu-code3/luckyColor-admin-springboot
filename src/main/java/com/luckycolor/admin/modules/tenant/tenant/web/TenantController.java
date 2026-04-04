package com.luckycolor.admin.modules.tenant.tenant.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantExpireTimeRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantSaveRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantStatusRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantDetailResponse;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/admin/tenants")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "Tenants", description = "Tenant management APIs")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/page")
    @RequirePermission("tenant:query")
    @Operation(summary = "Page tenants")
    public ApiResponse<PageResult<TenantPageResponse>> page(@ParameterObject TenantPageQuery query) {
        return ApiResponse.success(tenantService.pageTenants(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("tenant:query")
    @Operation(summary = "Get tenant detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Tenant not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Tenant not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<TenantDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(tenantService.getTenant(id));
    }

    @PostMapping
    @RequirePermission("tenant:create")
    @Operation(summary = "Create tenant")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"name must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Long> create(@Valid @RequestBody TenantSaveRequest request) {
        return ApiResponse.success(tenantService.createTenant(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("tenant:update")
    @Operation(summary = "Update tenant")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody TenantSaveRequest request) {
        tenantService.updateTenant(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("tenant:update")
    @Operation(summary = "Update tenant status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody TenantStatusRequest request) {
        tenantService.updateTenantStatus(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/expire-time")
    @RequirePermission("tenant:update")
    @Operation(summary = "Update tenant expiration time")
    public ApiResponse<Boolean> updateExpireTime(
        @PathVariable Long id,
        @Valid @RequestBody TenantExpireTimeRequest request
    ) {
        tenantService.updateTenantExpireTime(id, request);
        return ApiResponse.success(true);
    }
}
