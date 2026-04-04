package com.luckycolor.admin.modules.platform.codegen.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.codegen.mapper.CodegenTableMapper;
import com.luckycolor.admin.modules.platform.codegen.service.CodegenMetadataService;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenColumnsSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenDiscoveryQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenImportRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTablePageQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTableSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenDiscoveryTableResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTableDetailResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTablePageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/codegen/tables")
@Validated
@ConditionalOnPersistenceEnabled
@Tag(name = "Code Generation", description = "Code generation metadata APIs")
public class CodegenMetadataController {

    private final CodegenMetadataService codegenMetadataService;

    public CodegenMetadataController(CodegenMetadataService codegenMetadataService) {
        this.codegenMetadataService = codegenMetadataService;
    }

    @GetMapping("/discovery")
    @RequirePermission("codegen:query")
    @Operation(summary = "Discover database tables for code generation")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Discovery table list loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<CodegenDiscoveryTableResponse>> discovery(@ParameterObject CodegenDiscoveryQuery query) {
        return ApiResponse.success(codegenMetadataService.pageDiscoveryTables(query));
    }

    @PostMapping("/import")
    @RequirePermission("codegen:create")
    @Operation(summary = "Import tables into code generation metadata")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tables imported successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"tableNames must not be empty\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Integer> importTables(
        @Parameter(hidden = true) Authentication authentication,
        @Valid @RequestBody CodegenImportRequest request
    ) {
        return ApiResponse.success(codegenMetadataService.importTables(getAuthenticatedUser(authentication).tenantId(), request));
    }

    @GetMapping("/page")
    @RequirePermission("codegen:query")
    @Operation(summary = "Page code generation tables")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Code generation tables loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<CodegenTablePageResponse>> page(@ParameterObject CodegenTablePageQuery query) {
        return ApiResponse.success(codegenMetadataService.pageTables(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("codegen:query")
    @Operation(summary = "Get code generation table detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Code generation table detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Code generation table not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Code generation table not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<CodegenTableDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(codegenMetadataService.getTable(id));
    }

    @PutMapping("/{id}")
    @RequirePermission("codegen:update")
    @Operation(summary = "Update code generation table metadata")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Code generation table metadata updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"moduleName must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
            responseCode = "404", description = "Code generation table not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Code generation table not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody CodegenTableSaveRequest request) {
        codegenMetadataService.updateTable(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/columns")
    @RequirePermission("codegen:update")
    @Operation(summary = "Update code generation columns")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Code generation columns updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"columns must not be empty\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
            responseCode = "404", description = "Code generation table not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"Code generation table not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<Boolean> updateColumns(@PathVariable Long id, @Valid @RequestBody CodegenColumnsSaveRequest request) {
        codegenMetadataService.updateColumns(id, request);
        return ApiResponse.success(true);
    }

    private JwtAuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }
}
