package com.luckycolor.admin.modules.system.menu.service.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSyncRequest {

    private List<MenuSyncItemRequest> menus = new ArrayList<>();
}
