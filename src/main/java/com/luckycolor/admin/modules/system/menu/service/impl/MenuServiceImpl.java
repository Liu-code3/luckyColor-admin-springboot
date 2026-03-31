package com.luckycolor.admin.modules.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@TenantIgnore
@ConditionalOnBean(MenuMapper.class)
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuTreeResponse> listMenuTree(MenuTreeQuery query) {
        List<MenuDO> menus = menuMapper.selectList(buildQueryWrapper(query));
        return buildTree(menus, 0L);
    }

    @Override
    public MenuDetailResponse getMenu(Long id) {
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found");
        }
        return toDetailResponse(menu);
    }

    private LambdaQueryWrapper<MenuDO> buildQueryWrapper(MenuTreeQuery query) {
        LambdaQueryWrapper<MenuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getMenuName()), MenuDO::getMenuName, query.getMenuName());
        queryWrapper.eq(query.getStatus() != null, MenuDO::getStatus, query.getStatus());
        queryWrapper.orderByAsc(MenuDO::getParentId)
            .orderByAsc(MenuDO::getSort)
            .orderByAsc(MenuDO::getId);
        return queryWrapper;
    }

    private List<MenuTreeResponse> buildTree(List<MenuDO> menus, Long parentId) {
        return menus.stream()
            .filter(menu -> normalizeParentId(menu.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(MenuDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuDO::getId, Comparator.nullsLast(Long::compareTo)))
            .map(menu -> toTreeResponse(menu, buildTree(menus, menu.getId())))
            .toList();
    }

    private MenuTreeResponse toTreeResponse(MenuDO menu, List<MenuTreeResponse> children) {
        return new MenuTreeResponse(
            menu.getId(),
            normalizeParentId(menu.getParentId()),
            menu.getMenuName(),
            menu.getMenuType(),
            menu.getRouteName(),
            menu.getRoutePath(),
            menu.getComponent(),
            menu.getPermissionCode(),
            splitCodes(menu.getRoleCodes()),
            menu.getIcon(),
            menu.getSort(),
            menu.getVisible(),
            menu.getKeepAlive(),
            menu.getAlwaysShow(),
            menu.getStatus(),
            children
        );
    }

    private MenuDetailResponse toDetailResponse(MenuDO menu) {
        return new MenuDetailResponse(
            menu.getId(),
            normalizeParentId(menu.getParentId()),
            menu.getMenuName(),
            menu.getMenuType(),
            menu.getRouteName(),
            menu.getRoutePath(),
            menu.getComponent(),
            menu.getPermissionCode(),
            splitCodes(menu.getRoleCodes()),
            menu.getIcon(),
            menu.getSort(),
            menu.getVisible(),
            menu.getKeepAlive(),
            menu.getAlwaysShow(),
            menu.getStatus(),
            menu.getRemark()
        );
    }

    private List<String> splitCodes(String roleCodes) {
        if (!StringUtils.hasText(roleCodes)) {
            return List.of();
        }
        return java.util.Arrays.stream(roleCodes.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
