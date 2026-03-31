package com.luckycolor.admin.modules.tenant.tenant.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.BaseDO;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_tenant")
public class TenantDO extends BaseDO {

    private String name;

    private Long packageId;

    private String contactName;

    private String contactMobile;

    private Integer accountCount;

    private LocalDateTime expireTime;

    private Integer status;
}
