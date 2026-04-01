package com.luckycolor.admin.modules.platform.i18n.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class I18nResourcePageQuery extends PageQuery {

    private String locale;

    private String namespace;

    private String resourceKey;

    private Integer status;
}
