package com.luckycolor.admin.modules.system.department.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
