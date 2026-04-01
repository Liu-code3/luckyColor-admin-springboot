package com.luckycolor.admin.modules.system.dictionary.type.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictionaryTypePageQuery extends PageQuery {

    private String typeCode;

    private String typeName;

    private Integer status;
}
