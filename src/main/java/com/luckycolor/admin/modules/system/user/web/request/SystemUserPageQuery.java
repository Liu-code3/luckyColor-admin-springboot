package com.luckycolor.admin.modules.system.user.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "System user paging query")
public class SystemUserPageQuery extends PageQuery {

    @Schema(description = "Filter by username", example = "admin")
    private String username;

    @Schema(description = "Filter by nickname", example = "System")
    private String nickname;

    @Schema(description = "Filter by mobile number", example = "13800138000")
    private String mobile;

    @Schema(description = "Filter by status: 0 enabled, 1 disabled", example = "0")
    private Integer status;
}
