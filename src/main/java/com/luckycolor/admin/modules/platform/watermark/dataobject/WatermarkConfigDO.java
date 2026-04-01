package com.luckycolor.admin.modules.platform.watermark.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.luckycolor.admin.infrastructure.persistence.dataobject.TenantBaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_watermark_config")
public class WatermarkConfigDO extends TenantBaseDO {

    private Integer enabled;

    private String content;

    private String color;

    private Integer fontSize;

    private Integer opacityPercent;

    private Integer rotateDegree;

    private Integer gapX;

    private Integer gapY;
}
