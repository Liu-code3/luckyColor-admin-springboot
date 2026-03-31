package com.luckycolor.admin.modules.system.menu.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    @RequirePermission("system:menu:create")
    public ApiResponse<Long> create(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(menuService.createMenu(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:update")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        menuService.updateMenu(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:menu:update")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody MenuStatusRequest request) {
        menuService.updateMenuStatus(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success(true);
    }
}
