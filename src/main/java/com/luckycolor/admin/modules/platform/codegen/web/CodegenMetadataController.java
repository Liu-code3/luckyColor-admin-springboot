package com.luckycolor.admin.modules.platform.codegen.web;

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
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(CodegenTableMapper.class)
public class CodegenMetadataController {

    private final CodegenMetadataService codegenMetadataService;

    public CodegenMetadataController(CodegenMetadataService codegenMetadataService) {
        this.codegenMetadataService = codegenMetadataService;
    }

    @GetMapping("/discovery")
    @RequirePermission("codegen:query")
    public ApiResponse<PageResult<CodegenDiscoveryTableResponse>> discovery(CodegenDiscoveryQuery query) {
        return ApiResponse.success(codegenMetadataService.pageDiscoveryTables(query));
    }

    @PostMapping("/import")
    @RequirePermission("codegen:create")
    public ApiResponse<Integer> importTables(
        Authentication authentication,
        @Valid @RequestBody CodegenImportRequest request
    ) {
        return ApiResponse.success(codegenMetadataService.importTables(getAuthenticatedUser(authentication).tenantId(), request));
    }

    @GetMapping("/page")
    @RequirePermission("codegen:query")
    public ApiResponse<PageResult<CodegenTablePageResponse>> page(CodegenTablePageQuery query) {
        return ApiResponse.success(codegenMetadataService.pageTables(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("codegen:query")
    public ApiResponse<CodegenTableDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(codegenMetadataService.getTable(id));
    }

    @PutMapping("/{id}")
    @RequirePermission("codegen:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody CodegenTableSaveRequest request) {
        codegenMetadataService.updateTable(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/columns")
    @RequirePermission("codegen:update")
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
