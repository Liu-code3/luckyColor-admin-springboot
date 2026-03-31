package com.luckycolor.admin.modules.iam.auth.web.response;

import java.util.List;

public record AuthRouteResponse(
    String code,
    String name,
    String path,
    String fullPath,
    String component,
    String redirect,
    String icon,
    boolean hidden,
    boolean alwaysShow,
    boolean keepAlive,
    List<String> permissions,
    List<AuthRouteResponse> children
) {
}
