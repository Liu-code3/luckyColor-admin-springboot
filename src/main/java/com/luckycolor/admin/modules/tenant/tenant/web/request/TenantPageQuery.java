package com.luckycolor.admin.modules.tenant.tenant.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantPageQuery extends PageQuery {

    private String name;

    private Long packageId;

    private Integer status;
}
