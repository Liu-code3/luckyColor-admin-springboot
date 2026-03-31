package com.luckycolor.admin.modules.system.user.web.response;

import java.util.List;

public record SystemUserExportPreviewResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String mobile,
    List<String> roleCodes,
    Integer status
) {
}
