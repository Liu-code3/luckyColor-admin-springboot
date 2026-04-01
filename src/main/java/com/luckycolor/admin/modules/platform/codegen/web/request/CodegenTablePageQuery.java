package com.luckycolor.admin.modules.platform.codegen.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodegenTablePageQuery extends PageQuery {

    private String physicalTableName;

    private String businessName;
}
