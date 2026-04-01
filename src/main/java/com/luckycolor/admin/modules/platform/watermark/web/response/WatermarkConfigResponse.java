package com.luckycolor.admin.modules.platform.watermark.web.response;

public record WatermarkConfigResponse(
    Long tenantId,
    Integer enabled,
    String content,
    String color,
    Integer fontSize,
    Integer opacityPercent,
    Integer rotateDegree,
    Integer gapX,
    Integer gapY
) {
}
