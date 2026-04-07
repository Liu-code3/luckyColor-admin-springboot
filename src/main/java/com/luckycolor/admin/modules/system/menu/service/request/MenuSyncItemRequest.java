package com.luckycolor.admin.modules.system.menu.service.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSyncItemRequest {

    private Long id;

    private Long parentId;

    private Integer sort;
}
