package com.luckycolor.admin.modules.tenant.bootstrap.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantBootstrapRecordPageQuery extends PageQuery {

    private Long tenantId;

    private String templateCode;

    @Min(0)
    @Max(2)
    private Integer status;
}
