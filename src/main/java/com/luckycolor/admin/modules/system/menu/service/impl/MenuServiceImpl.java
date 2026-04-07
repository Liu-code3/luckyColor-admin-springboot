package com.luckycolor.admin.modules.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.service.request.MenuSyncItemRequest;
import com.luckycolor.admin.modules.system.menu.service.request.MenuSyncRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@TenantIgnore
@ConditionalOnPersistenceEnabled
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final ObjectMapper objectMapper;

    public MenuServiceImpl(MenuMapper menuMapper, ObjectMapper objectMapper) {
        this.menuMapper = menuMapper;
        this.objectMapper = objectMapper;
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
    public void syncMenus(MenuSyncRequest request) {
        List<MenuDO> currentMenus = menuMapper.selectList(buildAllMenusQuery());
        Map<Long, MenuDO> menusById = currentMenus.stream().collect(Collectors.toMap(MenuDO::getId, item -> item));
        validateSyncRequest(request.getMenus(), menusById);
        for (MenuSyncItemRequest item : request.getMenus()) {
            MenuDO menu = menusById.get(item.getId());
            menu.setParentId(normalizeParentId(item.getParentId()));
            menu.setSort(item.getSort());
            menuMapper.updateById(menu);
        }
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

    private LambdaQueryWrapper<MenuDO> buildAllMenusQuery() {
        LambdaQueryWrapper<MenuDO> queryWrapper = new LambdaQueryWrapper<>();
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
            menu.getMenuKey(),
            menu.getComponent(),
            menu.getRedirect(),
            parseMeta(menu.getMeta()),
            menu.getPermissionCode(),
            splitCodes(menu.getRoleCodes()),
            menu.getIcon(),
            menu.getLayout(),
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
            menu.getMenuKey(),
            menu.getComponent(),
            menu.getRedirect(),
            parseMeta(menu.getMeta()),
            menu.getPermissionCode(),
            splitCodes(menu.getRoleCodes()),
            menu.getIcon(),
            menu.getLayout(),
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

    private void validateSyncRequest(List<MenuSyncItemRequest> items, Map<Long, MenuDO> menusById) {
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (MenuSyncItemRequest item : items) {
            if (!uniqueIds.add(item.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate menu id in sync payload");
            }
            if (!menusById.containsKey(item.getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found");
            }
        }
        Map<Long, Long> nextParentIds = menusById.values().stream()
            .collect(Collectors.toMap(MenuDO::getId, menu -> normalizeParentId(menu.getParentId())));
        for (MenuSyncItemRequest item : items) {
            Long normalizedParentId = normalizeParentId(item.getParentId());
            if (!normalizedParentId.equals(0L) && !menusById.containsKey(normalizedParentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent menu not found");
            }
            nextParentIds.put(item.getId(), normalizedParentId);
        }
        for (Long menuId : nextParentIds.keySet()) {
            assertNoMenuCycle(menuId, nextParentIds);
        }
    }

    private void assertNoMenuCycle(Long menuId, Map<Long, Long> nextParentIds) {
        Set<Long> visited = new HashSet<>();
        Long currentId = menuId;
        while (true) {
            Long parentId = normalizeParentId(nextParentIds.get(currentId));
            if (parentId.equals(0L)) {
                return;
            }
            if (!visited.add(parentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu hierarchy cycle detected");
            }
            if (!nextParentIds.containsKey(parentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent menu not found");
            }
            currentId = parentId;
        }
    }

    private void fillMenu(MenuDO menu, MenuSaveRequest request) {
        menu.setParentId(normalizeParentId(request.getParentId()));
        menu.setMenuName(request.getMenuName());
        menu.setMenuType(request.getMenuType());
        menu.setRouteName(request.getRouteName());
        menu.setRoutePath(request.getRoutePath());
        menu.setMenuKey(request.getMenuKey());
        menu.setComponent(request.getComponent());
        menu.setRedirect(request.getRedirect());
        menu.setMeta(writeMeta(request.getMeta()));
        menu.setPermissionCode(request.getPermissionCode());
        menu.setRoleCodes(joinCodes(request.getRoleCodes()));
        menu.setIcon(request.getIcon());
        menu.setLayout(request.getLayout());
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setKeepAlive(request.getKeepAlive());
        menu.setAlwaysShow(request.getAlwaysShow());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
    }

    private java.util.Map<String, Object> parseMeta(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, new TypeReference<java.util.Map<String, Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse menu meta", exception);
        }
    }

    private String writeMeta(java.util.Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to serialize menu meta", exception);
        }
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
