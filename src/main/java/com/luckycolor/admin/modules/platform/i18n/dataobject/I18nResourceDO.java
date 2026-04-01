package com.luckycolor.admin.modules.platform.i18n.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_i18n_resource")
public class I18nResourceDO extends TenantBaseDO {

    private String locale;

    private String namespace;

    private String resourceKey;

    private String resourceValue;

    private Integer version;

    private Integer status;

    private String remark;
}
