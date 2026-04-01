package com.luckycolor.admin.modules.system.department.web.response;

public record SystemDepartmentDetailResponse(
    Long id,
    Long tenantId,
    Long parentId,
    String departmentName,
    String leader,
    String phone,
    String email,
    Integer sort,
    Integer status,
    String remark
) {
}
