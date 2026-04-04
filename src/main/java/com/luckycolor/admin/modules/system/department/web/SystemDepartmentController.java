package com.luckycolor.admin.modules.system.department.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentSaveRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentStatusRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
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
@RequestMapping("/admin/departments")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "System Departments", description = "Department management APIs")
public class SystemDepartmentController {

    private final SystemDepartmentService systemDepartmentService;

    public SystemDepartmentController(SystemDepartmentService systemDepartmentService) {
        this.systemDepartmentService = systemDepartmentService;
    }

    @GetMapping("/tree")
    @RequirePermission("system:department:query")
    @Operation(summary = "List department tree")
    public ApiResponse<List<SystemDepartmentTreeResponse>> tree(@ParameterObject SystemDepartmentTreeQuery query) {
        return ApiResponse.success(systemDepartmentService.listDepartmentTree(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:department:query")
    @Operation(summary = "Get department detail")
    public ApiResponse<SystemDepartmentDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemDepartmentService.getDepartment(id));
    }

    @PostMapping
    @RequirePermission("system:department:create")
    @Operation(summary = "Create department")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"departmentName must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Long> create(@Valid @RequestBody SystemDepartmentSaveRequest request) {
        return ApiResponse.success(systemDepartmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:department:update")
    @Operation(summary = "Update department")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemDepartmentSaveRequest request) {
        systemDepartmentService.updateDepartment(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:department:update")
    @Operation(summary = "Update department status")
    public ApiResponse<Boolean> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody SystemDepartmentStatusRequest request
    ) {
        systemDepartmentService.updateDepartmentStatus(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:department:delete")
    @Operation(summary = "Delete department")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        systemDepartmentService.deleteDepartment(id);
        return ApiResponse.success(true);
    }
}
