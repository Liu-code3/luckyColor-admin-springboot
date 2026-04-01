package com.luckycolor.admin.modules.system.department.web.response;

import java.util.List;

public record SystemDepartmentTreeResponse(
    Long id,
    Long tenantId,
    Long parentId,
    String departmentName,
    String leader,
    String phone,
    String email,
    Integer sort,
    Integer status,
    List<SystemDepartmentTreeResponse> children
) {
}
