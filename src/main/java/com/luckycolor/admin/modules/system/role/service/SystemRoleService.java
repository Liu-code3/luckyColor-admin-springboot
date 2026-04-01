package com.luckycolor.admin.modules.system.role.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleAuthorityRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRolePageQuery;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleSaveRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleStatusRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleDetailResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRolePageResponse;

public interface SystemRoleService {

    PageResult<SystemRolePageResponse> pageRoles(SystemRolePageQuery query);

    SystemRoleDetailResponse getRole(Long id);

    Long createRole(SystemRoleSaveRequest request);

    void updateRole(Long id, SystemRoleSaveRequest request);

    void updateRoleStatus(Long id, SystemRoleStatusRequest request);

    SystemRoleAuthorityResponse getRoleAuthority(Long id);

    void updateRoleAuthority(Long id, SystemRoleAuthorityRequest request);
}
