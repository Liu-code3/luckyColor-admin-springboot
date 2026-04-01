package com.luckycolor.admin.modules.system.role.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_role")
public class SystemRoleDO extends TenantBaseDO {

    private String roleCode;

    private String roleName;

    private Integer status;

    private Integer sort;

    private String menuIds;

    private String permissionCodes;

    private String dataScope;

    private Long departmentId;

    private String departmentIds;

    private String remark;
}
