package com.luckycolor.admin.modules.system.department.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_department")
public class SystemDepartmentDO extends TenantBaseDO {

    private Long parentId;

    private String departmentName;

    private String leader;

    private String phone;

    private String email;

    private Integer sort;

    private Integer status;

    private String remark;
}
