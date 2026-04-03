package com.luckycolor.admin.modules.system.menu.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSaveRequest {

    private Long parentId = 0L;

    @NotBlank
    private String menuName;

    @NotBlank
    private String menuType;

    private String routeName;

    private String routePath;

    private String menuKey;

    private String component;

    private String redirect;

    private java.util.Map<String, Object> meta = new java.util.LinkedHashMap<>();

    private String permissionCode;

    private List<String> roleCodes = new ArrayList<>();

    private String icon;

    private String layout;

    @NotNull
    private Integer sort;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer visible;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer keepAlive;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer alwaysShow;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;

    private String remark;
}
