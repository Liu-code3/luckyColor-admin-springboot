package com.luckycolor.admin.modules.tenant.bootstrap.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.tenant.bootstrap.mapper.TenantBootstrapRecordMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapExecuteRequest;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapRecordPageQuery;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@ConditionalOnPersistenceEnabled
@Tag(name = "Tenant Bootstrap", description = "Tenant bootstrap template and execution APIs")
public class TenantBootstrapController {

    private final TenantBootstrapService tenantBootstrapService;

    public TenantBootstrapController(TenantBootstrapService tenantBootstrapService) {
        this.tenantBootstrapService = tenantBootstrapService;
    }

    @GetMapping("/admin/tenant-bootstrap/templates")
    @RequirePermission("tenant:bootstrap:query")
    @Operation(summary = "List tenant bootstrap templates")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant bootstrap templates loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<List<TenantBootstrapTemplateResponse>> templates() {
        return ApiResponse.success(tenantBootstrapService.listTemplates());
    }

    @GetMapping("/admin/tenant-bootstrap/records/page")
    @RequirePermission("tenant:bootstrap:query")
    @Operation(summary = "Page tenant bootstrap records")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant bootstrap records loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<TenantBootstrapRecordResponse>> page(@ParameterObject TenantBootstrapRecordPageQuery query) {
        return ApiResponse.success(tenantBootstrapService.pageRecords(query));
    }

    @PostMapping("/admin/tenants/{tenantId}/bootstrap")
    @RequirePermission("tenant:bootstrap:execute")
    @Operation(summary = "Bootstrap a tenant")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant bootstrapped successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"templateCode must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<TenantBootstrapRecordResponse> bootstrap(
        @PathVariable Long tenantId,
        @Valid @RequestBody TenantBootstrapExecuteRequest request
    ) {
        return ApiResponse.success(tenantBootstrapService.bootstrapTenant(tenantId, request));
    }
}
