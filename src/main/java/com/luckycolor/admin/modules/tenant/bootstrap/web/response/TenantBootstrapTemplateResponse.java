package com.luckycolor.admin.modules.tenant.bootstrap.web.response;

import java.util.List;

public record TenantBootstrapTemplateResponse(
    String code,
    String name,
    List<String> roleCodes,
    List<String> menuCodes,
    String adminUsername,
    String adminNickname,
    Integer status,
    String remark
) {
}
