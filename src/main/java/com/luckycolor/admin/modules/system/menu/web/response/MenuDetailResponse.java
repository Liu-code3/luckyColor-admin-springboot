package com.luckycolor.admin.modules.system.menu.web.response;

import java.util.List;

public record MenuDetailResponse(
    Long id,
    Long parentId,
    String menuName,
    String menuType,
    String routeName,
    String routePath,
    String menuKey,
    String component,
    String redirect,
    java.util.Map<String, Object> meta,
    String permissionCode,
    List<String> roleCodes,
    String icon,
    String layout,
    Integer sort,
    Integer visible,
    Integer keepAlive,
    Integer alwaysShow,
    Integer status,
    String remark
) {
}
