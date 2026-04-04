package com.luckycolor.admin.modules.system.user.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.REQUEST_PARAMETER_INVALID;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.USERNAME_ALREADY_EXISTS;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserAssignRolesRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserResetPasswordRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserStatusRequest;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/users")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "System Users", description = "System user management APIs")
public class SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @GetMapping("/page")
    @RequirePermission("system:user:query")
    @Operation(summary = "Page system users")
    public ApiResponse<PageResult<SystemUserPageResponse>> page(@ParameterObject SystemUserPageQuery query) {
        return ApiResponse.success(systemUserService.pageUsers(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    @Operation(summary = "Get system user detail")
    public ApiResponse<SystemUserDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemUserService.getUser(id));
    }

    @GetMapping("/role-options")
    @RequirePermission("system:user:query")
    @Operation(summary = "List available role options")
    public ApiResponse<List<String>> roleOptions() {
        return ApiResponse.success(systemUserService.listRoleOptions());
    }

    @GetMapping("/export-preview")
    @RequirePermission("system:user:query")
    @Operation(summary = "Preview exported system users")
    public ApiResponse<List<SystemUserExportPreviewResponse>> exportPreview(@ParameterObject SystemUserPageQuery query) {
        return ApiResponse.success(systemUserService.listUsersForExportPreview(query));
    }

    @PostMapping
    @RequirePermission("system:user:create")
    @Operation(summary = "Create system user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = "{\"code\":0,\"message\":\"ok\",\"data\":1001,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = "{\"code\":400,\"message\":\"username must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = FORBIDDEN
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Username already exists",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = USERNAME_ALREADY_EXISTS
            ))
        )
    })
    public ApiResponse<Long> create(@Valid @RequestBody SystemUserSaveRequest request) {
        return ApiResponse.success(systemUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:update")
    @Operation(summary = "Update system user")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemUserSaveRequest request) {
        systemUserService.updateUser(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:update")
    @Operation(summary = "Update system user status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody SystemUserStatusRequest request) {
        systemUserService.updateUserStatus(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    @Operation(summary = "Delete system user")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        systemUserService.deleteUser(id);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/password")
    @RequirePermission("system:user:reset-password")
    @Operation(summary = "Reset system user password")
    public ApiResponse<Boolean> resetPassword(
        @PathVariable Long id,
        @Valid @RequestBody SystemUserResetPasswordRequest request
    ) {
        systemUserService.resetPassword(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("system:user:assign-role")
    @Operation(summary = "Assign roles to system user")
    public ApiResponse<Boolean> assignRoles(
        @PathVariable Long id,
        @Valid @RequestBody SystemUserAssignRolesRequest request
    ) {
        systemUserService.assignRoles(id, request);
        return ApiResponse.success(true);
    }

    @GetMapping("/export")
    @RequirePermission("system:user:export")
    @Operation(summary = "Export system users as CSV")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "CSV export stream",
            content = @Content(
                mediaType = "text/csv",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = FORBIDDEN
            ))
        )
    })
    public ResponseEntity<ByteArrayResource> export(@ParameterObject SystemUserPageQuery query) {
        byte[] content = systemUserService.exportUsers(query);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=system-users.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .contentLength(content.length)
            .body(new ByteArrayResource(content));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("system:user:import")
    @Operation(summary = "Import system users from file")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Import completed successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = "{\"code\":0,\"message\":\"ok\",\"data\":12,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Missing file or invalid import file",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "missing-file", value = REQUEST_PARAMETER_INVALID),
                @ExampleObject(name = "invalid-file", value = "{\"code\":400,\"message\":\"Failed to import users\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}")
            })
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = UNAUTHORIZED
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                value = FORBIDDEN
            ))
        )
    })
    public ApiResponse<Integer> importUsers(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(systemUserService.importUsers(file));
    }
}
