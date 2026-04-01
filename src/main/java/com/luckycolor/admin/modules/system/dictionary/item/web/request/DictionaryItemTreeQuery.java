package com.luckycolor.admin.modules.system.dictionary.item.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictionaryItemTreeQuery {

    private String typeCode;

    private String itemLabel;

    private Integer status;
}
