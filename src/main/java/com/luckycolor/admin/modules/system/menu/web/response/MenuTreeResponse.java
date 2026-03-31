package com.luckycolor.admin.modules.system.menu.web.response;

import java.util.List;

public record MenuTreeResponse(
    Long id,
    Long parentId,
    String menuName,
    String menuType,
    String routeName,
    String routePath,
    String component,
    String permissionCode,
    List<String> roleCodes,
    String icon,
    Integer sort,
    Integer visible,
    Integer keepAlive,
    Integer alwaysShow,
    Integer status,
    List<MenuTreeResponse> children
) {
}
