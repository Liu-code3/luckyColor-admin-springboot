package com.luckycolor.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TenantBaseDO extends BaseDO {

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
