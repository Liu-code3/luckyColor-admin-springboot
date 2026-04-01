package com.luckycolor.admin.modules.system.role.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemRolePageQuery extends PageQuery {

    private String roleCode;

    private String roleName;

    private Integer status;
}
