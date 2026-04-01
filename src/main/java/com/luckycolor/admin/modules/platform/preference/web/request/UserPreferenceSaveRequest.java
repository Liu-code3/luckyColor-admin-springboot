package com.luckycolor.admin.modules.platform.preference.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPreferenceSaveRequest {

    @NotBlank
    private String themeScheme;

    @NotBlank
    private String themeColor;

    @NotBlank
    private String layoutMode;

    @NotBlank
    private String contentWidth;

    @NotNull
    private Integer tabBar;

    @NotNull
    private Integer fixedHeader;

    @NotNull
    private Integer fixedSidebar;

    @NotNull
    private Integer sidebarCollapsed;

    @NotNull
    private Integer compactMode;

    @NotBlank
    private String locale;
}
