package com.luckycolor.admin.modules.system.menu.service;

import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;

public interface MenuService {

    List<MenuTreeResponse> listMenuTree(MenuTreeQuery query);

    MenuDetailResponse getMenu(Long id);

    Long createMenu(MenuSaveRequest request);

    void updateMenu(Long id, MenuSaveRequest request);

    void updateMenuStatus(Long id, MenuStatusRequest request);

    void deleteMenu(Long id);
}
