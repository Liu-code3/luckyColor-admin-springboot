package com.luckycolor.admin.modules.system.department.service;

import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.List;

public interface SystemDepartmentService {

    List<SystemDepartmentTreeResponse> listDepartmentTree(SystemDepartmentTreeQuery query);

    SystemDepartmentDetailResponse getDepartment(Long id);
}
