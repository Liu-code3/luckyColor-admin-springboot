package com.luckycolor.admin.modules.system.user.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemUserPageQuery extends PageQuery {

    private String username;

    private String nickname;

    private String mobile;

    private Integer status;
}
