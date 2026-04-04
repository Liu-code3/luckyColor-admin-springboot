package com.luckycolor.admin.modules.tenant.tenant.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Tenant paging query")
public class TenantPageQuery extends PageQuery {

    @Schema(description = "Filter by tenant name", example = "LuckyColor")
    private String name;

    @Schema(description = "Filter by package ID", example = "10")
    private Long packageId;

    @Schema(description = "Filter by status: 0 enabled, 1 disabled", example = "0")
    private Integer status;
}
