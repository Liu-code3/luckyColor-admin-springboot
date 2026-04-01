package com.luckycolor.admin.modules.platform.watermark.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WatermarkConfigSaveRequest {

    @NotNull
    @Min(0)
    @Max(1)
    private Integer enabled;

    @NotBlank
    private String content;

    @NotBlank
    private String color;

    @NotNull
    @Min(10)
    @Max(96)
    private Integer fontSize;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer opacityPercent;

    @NotNull
    @Min(-180)
    @Max(180)
    private Integer rotateDegree;

    @NotNull
    @Min(40)
    @Max(400)
    private Integer gapX;

    @NotNull
    @Min(40)
    @Max(400)
    private Integer gapY;
}
