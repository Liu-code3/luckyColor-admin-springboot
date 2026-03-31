package com.luckycolor.admin.modules.tenant.packageinfo.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_tenant_package")
public class TenantPackageDO extends BaseDO {

    private String packageName;

    private Integer status;

    private Integer sort;

    private String remark;
}
