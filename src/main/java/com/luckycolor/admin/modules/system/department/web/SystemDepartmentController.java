package com.luckycolor.admin.modules.system.department.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentSaveRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentStatusRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(SystemDepartmentMapper.class)
@Validated
public class SystemDepartmentController {

    private final SystemDepartmentService systemDepartmentService;

    public SystemDepartmentController(SystemDepartmentService systemDepartmentService) {
        this.systemDepartmentService = systemDepartmentService;
    }

    @GetMapping("/tree")
    @RequirePermission("system:department:query")
    public ApiResponse<List<SystemDepartmentTreeResponse>> tree(SystemDepartmentTreeQuery query) {
        return ApiResponse.success(systemDepartmentService.listDepartmentTree(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:department:query")
    public ApiResponse<SystemDepartmentDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemDepartmentService.getDepartment(id));
    }

    @PostMapping
    @RequirePermission("system:department:create")
    public ApiResponse<Long> create(@Valid @RequestBody SystemDepartmentSaveRequest request) {
        return ApiResponse.success(systemDepartmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:department:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemDepartmentSaveRequest request) {
        systemDepartmentService.updateDepartment(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:department:update")
    public ApiResponse<Boolean> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody SystemDepartmentStatusRequest request
    ) {
        systemDepartmentService.updateDepartmentStatus(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:department:delete")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        systemDepartmentService.deleteDepartment(id);
        return ApiResponse.success(true);
    }
}
