package com.luckycolor.admin.modules.system.role.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.role.service.SystemRoleService;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleAuthorityRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRolePageQuery;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleSaveRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleStatusRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleDetailResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRolePageResponse;
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
@RequestMapping("/admin/roles")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "System Roles", description = "Role and permission assignment APIs")
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    public SystemRoleController(SystemRoleService systemRoleService) {
        this.systemRoleService = systemRoleService;
    }

    @GetMapping("/page")
    @RequirePermission("system:role:query")
    @Operation(summary = "Page system roles")
    public ApiResponse<PageResult<SystemRolePageResponse>> page(@ParameterObject SystemRolePageQuery query) {
        return ApiResponse.success(systemRoleService.pageRoles(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    @Operation(summary = "Get system role detail")
    public ApiResponse<SystemRoleDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemRoleService.getRole(id));
    }

    @PostMapping
    @RequirePermission("system:role:create")
    @Operation(summary = "Create system role")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"roleName must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Long> create(@Valid @RequestBody SystemRoleSaveRequest request) {
        return ApiResponse.success(systemRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:update")
    @Operation(summary = "Update system role")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemRoleSaveRequest request) {
        systemRoleService.updateRole(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:role:update")
    @Operation(summary = "Update system role status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody SystemRoleStatusRequest request) {
        systemRoleService.updateRoleStatus(id, request);
        return ApiResponse.success(true);
    }

    @GetMapping("/{id}/authority")
    @RequirePermission("system:role:query")
    @Operation(summary = "Get role authority snapshot")
    public ApiResponse<SystemRoleAuthorityResponse> authority(@PathVariable Long id) {
        return ApiResponse.success(systemRoleService.getRoleAuthority(id));
    }

    @PutMapping("/{id}/authority")
    @RequirePermission("system:role:authorize")
    @Operation(summary = "Update role authority")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role authority updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Boolean> updateAuthority(
        @PathVariable Long id,
        @Valid @RequestBody SystemRoleAuthorityRequest request
    ) {
        systemRoleService.updateRoleAuthority(id, request);
        return ApiResponse.success(true);
    }
}
