package com.luckycolor.admin.modules.system.user.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserStatusRequest;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.List;

public interface SystemUserService {

    PageResult<SystemUserPageResponse> pageUsers(SystemUserPageQuery query);

    SystemUserDetailResponse getUser(Long id);

    List<String> listRoleOptions();

    List<SystemUserExportPreviewResponse> listUsersForExportPreview(SystemUserPageQuery query);

    Long createUser(SystemUserSaveRequest request);

    void updateUser(Long id, SystemUserSaveRequest request);

    void updateUserStatus(Long id, SystemUserStatusRequest request);

    void deleteUser(Long id);
}
