package com.luckycolor.admin.modules.system.menu.service;

import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;

public interface MenuService {

    List<MenuTreeResponse> listMenuTree(MenuTreeQuery query);

    MenuDetailResponse getMenu(Long id);
}
