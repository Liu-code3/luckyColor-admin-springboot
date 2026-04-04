package com.luckycolor.admin.modules.system.config.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_config")
public class SystemConfigDO extends TenantBaseDO {

    private String configKey;

    private String configName;

    private String configValue;

    @TableField("sensitive_flag")
    private Integer sensitive;

    private Integer status;

    private Integer sort;

    private String remark;
}
