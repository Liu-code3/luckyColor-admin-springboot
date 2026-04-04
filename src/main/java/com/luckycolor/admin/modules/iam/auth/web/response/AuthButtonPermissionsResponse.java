package com.luckycolor.admin.modules.iam.auth.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Current user button permission snapshot")
public record AuthButtonPermissionsResponse(
    @Schema(description = "Available button permission codes")
    List<String> buttonCodeList,
    @Schema(description = "Granted codes from the requested list")
    List<String> grantedCodeList,
    @Schema(description = "Requested permission lookup map")
    Map<String, Boolean> permissionMap
) {
}
