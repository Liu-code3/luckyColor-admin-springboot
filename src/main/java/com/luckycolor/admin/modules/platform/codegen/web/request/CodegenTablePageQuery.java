package com.luckycolor.admin.modules.platform.codegen.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Code generation table paging query")
public class CodegenTablePageQuery extends PageQuery {

    @Schema(description = "Filter by physical table name", example = "sys_user")
    private String physicalTableName;

    @Schema(description = "Filter by business name", example = "System User")
    private String businessName;
}
