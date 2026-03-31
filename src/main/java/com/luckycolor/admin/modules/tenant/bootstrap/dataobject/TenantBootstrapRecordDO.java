package com.luckycolor.admin.modules.tenant.bootstrap.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.BaseDO;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_tenant_bootstrap_record")
public class TenantBootstrapRecordDO extends BaseDO {

    private Long tenantId;

    private String templateCode;

    private String templateName;

    private String roleCodes;

    private String menuCodes;

    private String adminUsername;

    private String adminNickname;

    private Integer status;

    private LocalDateTime bootstrapTime;

    private String remark;
}
