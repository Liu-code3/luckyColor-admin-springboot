package com.luckycolor.admin.modules.platform.preference.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user_preference")
public class UserPreferenceDO extends TenantBaseDO {

    private Long userId;

    private String themeScheme;

    private String themeColor;

    private String layoutMode;

    private String contentWidth;

    private Integer tabBar;

    private Integer fixedHeader;

    private Integer fixedSidebar;

    private Integer sidebarCollapsed;

    private Integer compactMode;

    private String locale;
}
