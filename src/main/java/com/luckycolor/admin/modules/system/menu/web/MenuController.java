package com.luckycolor.admin.modules.system.menu.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/menus")
@ConditionalOnBean(MenuMapper.class)
@Validated
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    @RequirePermission("system:menu:query")
    public ApiResponse<List<MenuTreeResponse>> tree(MenuTreeQuery query) {
        return ApiResponse.success(menuService.listMenuTree(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:menu:query")
    public ApiResponse<MenuDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(menuService.getMenu(id));
    }
}
