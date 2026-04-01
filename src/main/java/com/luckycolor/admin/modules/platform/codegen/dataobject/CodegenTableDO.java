package com.luckycolor.admin.modules.platform.codegen.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_codegen_table")
public class CodegenTableDO extends TenantBaseDO {

    private String physicalTableName;

    private String tableComment;

    private String businessName;

    private String className;

    private String moduleName;

    private String packageName;

    private String genMode;

    private Integer columnCount;

    private String remark;
}
