package com.luckycolor.admin.modules.system.config.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemConfigPageQuery extends PageQuery {

    private String configKey;

    private String configName;

    private Integer sensitive;

    private Integer status;
}
