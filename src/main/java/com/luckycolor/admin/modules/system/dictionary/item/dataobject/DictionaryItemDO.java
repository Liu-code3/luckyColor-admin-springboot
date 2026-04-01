package com.luckycolor.admin.modules.system.dictionary.item.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_dictionary_item")
public class DictionaryItemDO extends TenantBaseDO {

    private String typeCode;

    private Long parentId;

    private String itemLabel;

    private String itemValue;

    private String itemTag;

    private Integer sort;

    private Integer status;

    private String remark;
}
