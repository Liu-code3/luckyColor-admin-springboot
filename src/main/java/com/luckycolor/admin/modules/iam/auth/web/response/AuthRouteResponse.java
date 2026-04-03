package com.luckycolor.admin.modules.iam.auth.web.response;

import java.util.Map;
import java.util.List;

public record AuthRouteResponse(
    String path,
    String name,
    String component,
    String redirect,
    Map<String, Object> meta,
    List<AuthRouteResponse> children
) {
}
