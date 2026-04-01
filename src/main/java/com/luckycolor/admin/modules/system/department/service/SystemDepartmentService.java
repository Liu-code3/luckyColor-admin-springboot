package com.luckycolor.admin.modules.system.department.service;

import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentSaveRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentStatusRequest;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.List;

public interface SystemDepartmentService {

    List<SystemDepartmentTreeResponse> listDepartmentTree(SystemDepartmentTreeQuery query);

    SystemDepartmentDetailResponse getDepartment(Long id);

    Long createDepartment(SystemDepartmentSaveRequest request);

    void updateDepartment(Long id, SystemDepartmentSaveRequest request);

    void updateDepartmentStatus(Long id, SystemDepartmentStatusRequest request);

    void deleteDepartment(Long id);
}
