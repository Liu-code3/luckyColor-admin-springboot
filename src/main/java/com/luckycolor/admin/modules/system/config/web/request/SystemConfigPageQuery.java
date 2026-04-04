package com.luckycolor.admin.modules.system.config.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "System config paging query")
public class SystemConfigPageQuery extends PageQuery {

    @Schema(description = "Filter by config key", example = "site.title")
    private String configKey;

    @Schema(description = "Filter by config name", example = "Site Title")
    private String configName;

    @Schema(description = "Filter by sensitivity flag: 0 no, 1 yes", example = "0")
    private Integer sensitive;

    @Schema(description = "Filter by status: 0 enabled, 1 disabled", example = "0")
    private Integer status;
}
