package com.luckycolor.admin.modules.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
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
        return toDetailResponse(getRequiredMenu(id));
    }

    @Override
    public Long createMenu(MenuSaveRequest request) {
        validateParentExists(request.getParentId());
        ensureRouteNameUnique(null, request.getRouteName());
        MenuDO menu = new MenuDO();
        fillMenu(menu, request);
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    public void updateMenu(Long id, MenuSaveRequest request) {
        MenuDO menu = getRequiredMenu(id);
        validateParentExists(request.getParentId());
        ensureRouteNameUnique(id, request.getRouteName());
        fillMenu(menu, request);
        menuMapper.updateById(menu);
    }

    @Override
    public void updateMenuStatus(Long id, MenuStatusRequest request) {
        MenuDO menu = getRequiredMenu(id);
        menu.setStatus(request.getStatus());
        menuMapper.updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        getRequiredMenu(id);
        LambdaQueryWrapper<MenuDO> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(MenuDO::getParentId, id);
        Long childCount = menuMapper.selectCount(childQuery);
        if (childCount != null && childCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu has children and cannot be deleted");
        }
        menuMapper.deleteById(id);
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

    private MenuDO getRequiredMenu(Long id) {
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found");
        }
        return menu;
    }

    private void validateParentExists(Long parentId) {
        if (normalizeParentId(parentId).equals(0L)) {
            return;
        }
        if (menuMapper.selectById(parentId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent menu not found");
        }
    }

    private void ensureRouteNameUnique(Long currentId, String routeName) {
        if (!StringUtils.hasText(routeName)) {
            return;
        }
        LambdaQueryWrapper<MenuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuDO::getRouteName, routeName.trim());
        List<MenuDO> existingMenus = menuMapper.selectList(queryWrapper);
        boolean duplicated = existingMenus.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Route name already exists");
        }
    }

    private void fillMenu(MenuDO menu, MenuSaveRequest request) {
        menu.setParentId(normalizeParentId(request.getParentId()));
        menu.setMenuName(request.getMenuName());
        menu.setMenuType(request.getMenuType());
        menu.setRouteName(request.getRouteName());
        menu.setRoutePath(request.getRoutePath());
        menu.setComponent(request.getComponent());
        menu.setPermissionCode(request.getPermissionCode());
        menu.setRoleCodes(joinCodes(request.getRoleCodes()));
        menu.setIcon(request.getIcon());
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setKeepAlive(request.getKeepAlive());
        menu.setAlwaysShow(request.getAlwaysShow());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
    }

    private String joinCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return null;
        }
        return roleCodes.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse(null);
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
