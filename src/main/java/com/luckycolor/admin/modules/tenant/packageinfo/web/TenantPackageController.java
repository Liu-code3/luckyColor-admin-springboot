package com.luckycolor.admin.modules.tenant.packageinfo.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageSaveRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageStatusRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackageDetailResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
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
@RequestMapping("/admin/tenant-packages")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "Tenant Packages", description = "Tenant package management APIs")
public class TenantPackageController {

    private final TenantPackageService tenantPackageService;

    public TenantPackageController(TenantPackageService tenantPackageService) {
        this.tenantPackageService = tenantPackageService;
    }

    @GetMapping("/page")
    @RequirePermission("tenant:package:query")
    @Operation(summary = "Page tenant packages")
    public ApiResponse<PageResult<TenantPackagePageResponse>> page(@ParameterObject TenantPackagePageQuery query) {
        return ApiResponse.success(tenantPackageService.pageTenantPackages(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("tenant:package:query")
    @Operation(summary = "Get tenant package detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant package detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<TenantPackageDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(tenantPackageService.getTenantPackage(id));
    }

    @PostMapping
    @RequirePermission("tenant:package:create")
    @Operation(summary = "Create tenant package")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant package created successfully"),
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
    public ApiResponse<Long> create(@Valid @RequestBody TenantPackageSaveRequest request) {
        return ApiResponse.success(tenantPackageService.createTenantPackage(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("tenant:package:update")
    @Operation(summary = "Update tenant package")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody TenantPackageSaveRequest request) {
        tenantPackageService.updateTenantPackage(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("tenant:package:update")
    @Operation(summary = "Update tenant package status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody TenantPackageStatusRequest request) {
        tenantPackageService.updateTenantPackageStatus(id, request);
        return ApiResponse.success(true);
    }
}
