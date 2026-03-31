package com.luckycolor.admin.modules.system.user.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user")
public class SystemUserDO extends TenantBaseDO {

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String mobile;

    private Integer status;

    private String roleCodes;

    private String permissionCodes;

    private String dataScope;

    private Long departmentId;

    private String departmentIds;

    private String scopeTenantIds;

    private String remark;
}
