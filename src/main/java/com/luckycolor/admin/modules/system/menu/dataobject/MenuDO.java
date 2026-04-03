package com.luckycolor.admin.modules.system.menu.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_menu")
public class MenuDO extends BaseDO {

    private Long parentId;

    private String menuName;

    private String menuType;

    private String routeName;

    private String routePath;

    private String menuKey;

    private String component;

    private String redirect;

    private String meta;

    private String permissionCode;

    private String roleCodes;

    private String icon;

    private String layout;

    private Integer sort;

    private Integer visible;

    private Integer keepAlive;

    private Integer alwaysShow;

    private Integer status;

    private String remark;
}
