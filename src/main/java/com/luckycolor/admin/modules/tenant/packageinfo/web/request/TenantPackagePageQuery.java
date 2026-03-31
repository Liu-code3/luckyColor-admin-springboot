package com.luckycolor.admin.modules.tenant.packageinfo.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantPackagePageQuery extends PageQuery {

    private String packageName;

    private Integer status;
}
