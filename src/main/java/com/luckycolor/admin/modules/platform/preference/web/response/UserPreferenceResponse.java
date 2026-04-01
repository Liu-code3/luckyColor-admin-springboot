package com.luckycolor.admin.modules.platform.preference.web.response;

public record UserPreferenceResponse(
    Long userId,
    Long tenantId,
    String themeScheme,
    String themeColor,
    String layoutMode,
    String contentWidth,
    Integer tabBar,
    Integer fixedHeader,
    Integer fixedSidebar,
    Integer sidebarCollapsed,
    Integer compactMode,
    String locale
) {
}
