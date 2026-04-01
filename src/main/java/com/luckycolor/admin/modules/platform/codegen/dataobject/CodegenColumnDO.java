package com.luckycolor.admin.modules.platform.codegen.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_codegen_column")
public class CodegenColumnDO extends TenantBaseDO {

    private Long tableId;

    private String columnName;

    private String columnComment;

    private String jdbcType;

    private String javaType;

    private String javaField;

    private String htmlType;

    private String queryType;

    private Integer required;

    private Integer listVisible;

    private Integer formVisible;

    private Integer status;
}
