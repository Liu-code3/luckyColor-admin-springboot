package com.luckycolor.admin.modules.system.dictionary.type.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_dictionary_type")
public class DictionaryTypeDO extends TenantBaseDO {

    private String typeCode;

    private String typeName;

    private Integer status;

    private Integer sort;

    private String remark;
}
