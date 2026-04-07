package com.luckycolor.admin.modules.system.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.impl.MenuServiceImpl;
import com.luckycolor.admin.modules.system.menu.service.request.MenuSyncItemRequest;
import com.luckycolor.admin.modules.system.menu.service.request.MenuSyncRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class MenuServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldReturnMenuTreeSortedByParentAndSort() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
            menu(2L, 1L, "System Role", 2),
            menu(1L, 0L, "System", 1),
            menu(3L, 1L, "System User", 1)
        ));
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        List<MenuTreeResponse> result = service.listMenuTree(new MenuTreeQuery());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuName()).isEqualTo("System");
        assertThat(result.get(0).children()).extracting(MenuTreeResponse::menuName)
            .containsExactly("System User", "System Role");
    }

    @Test
    void shouldReturnMenuDetail() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        MenuDO menu = menu(1L, 0L, "Dashboard", 1);
        menu.setRoleCodes("ROLE_SUPER_ADMIN,ROLE_ADMIN");
        menu.setRemark("default");
        when(mapper.selectById(1L)).thenReturn(menu);
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        MenuDetailResponse result = service.getMenu(1L);

        assertThat(result.menuName()).isEqualTo("Dashboard");
        assertThat(result.menuKey()).isEqualTo("menu:1");
        assertThat(result.layout()).isEqualTo("default");
        assertThat(result.meta()).containsEntry("title", "Dashboard");
        assertThat(result.roleCodes()).containsExactly("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
        assertThat(result.remark()).isEqualTo("default");
    }

    @Test
    void shouldThrowWhenMenuNotFound() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        assertThatThrownBy(() -> service.getMenu(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void shouldCreateMenu() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectById(0L)).thenReturn(null);
        when(mapper.selectList(any())).thenReturn(List.of());
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        Long result = service.createMenu(buildSaveRequest());

        assertThat(result).isNull();
        ArgumentCaptor<MenuDO> captor = ArgumentCaptor.forClass(MenuDO.class);
        verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getMenuKey()).isEqualTo("main_system_users");
        assertThat(captor.getValue().getRedirect()).isEqualTo("/system/users/list");
        assertThat(captor.getValue().getLayout()).isEqualTo("default");
        assertThat(captor.getValue().getMeta()).contains("\"title\":\"System User\"");
        assertThat(captor.getValue().getMeta()).contains("\"keepAlive\":true");
    }

    @Test
    void shouldUpdateMenuStatus() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        MenuDO menu = menu(1L, 0L, "Dashboard", 1);
        when(mapper.selectById(1L)).thenReturn(menu);
        MenuService service = new MenuServiceImpl(mapper, objectMapper);
        MenuStatusRequest request = new MenuStatusRequest();
        request.setStatus(1);

        service.updateMenuStatus(1L, request);

        assertThat(menu.getStatus()).isEqualTo(1);
        verify(mapper).updateById(menu);
    }

    @Test
    void shouldSyncMenus() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        MenuDO dashboard = menu(1L, 0L, "Dashboard", 1);
        MenuDO system = menu(10L, 0L, "System", 2);
        MenuDO users = menu(11L, 10L, "System User", 1);
        when(mapper.selectList(any())).thenReturn(List.of(dashboard, system, users));
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        MenuSyncRequest request = new MenuSyncRequest();
        MenuSyncItemRequest item = new MenuSyncItemRequest();
        item.setId(11L);
        item.setParentId(0L);
        item.setSort(3);
        request.setMenus(List.of(item));

        service.syncMenus(request);

        assertThat(users.getParentId()).isEqualTo(0L);
        assertThat(users.getSort()).isEqualTo(3);
        verify(mapper).updateById(users);
    }

    @Test
    void shouldRejectDeleteWhenMenuHasChildren() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectById(1L)).thenReturn(menu(1L, 0L, "System", 1));
        when(mapper.selectCount(any())).thenReturn(1L);
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        assertThatThrownBy(() -> service.deleteMenu(1L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        verify(mapper, never()).deleteById(eq(1L));
    }

    @Test
    void shouldDeleteMenuWithoutChildren() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectById(1L)).thenReturn(menu(1L, 0L, "System", 1));
        when(mapper.selectCount(any())).thenReturn(0L);
        MenuService service = new MenuServiceImpl(mapper, objectMapper);

        service.deleteMenu(1L);

        verify(mapper).deleteById(1L);
    }

    private MenuDO menu(Long id, Long parentId, String menuName, Integer sort) {
        MenuDO menu = new MenuDO();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setMenuType("MENU");
        menu.setRouteName(menuName.replace(" ", ""));
        menu.setRoutePath("/" + menuName.toLowerCase().replace(" ", "-"));
        menu.setMenuKey("menu:" + id);
        menu.setComponent("system/" + id);
        menu.setRedirect(null);
        menu.setMeta("{\"title\":\"" + menuName + "\",\"keepAlive\":false,\"hidden\":false}");
        menu.setPermissionCode("system:menu:query");
        menu.setSort(sort);
        menu.setLayout("default");
        menu.setVisible(1);
        menu.setKeepAlive(0);
        menu.setAlwaysShow(0);
        menu.setStatus(0);
        return menu;
    }

    private MenuSaveRequest buildSaveRequest() {
        MenuSaveRequest request = new MenuSaveRequest();
        request.setParentId(0L);
        request.setMenuName("System User");
        request.setMenuType("MENU");
        request.setRouteName("SystemUser");
        request.setRoutePath("users");
        request.setMenuKey("main_system_users");
        request.setComponent("system/user/index");
        request.setRedirect("/system/users/list");
        request.setMeta(java.util.Map.of("title", "System User", "keepAlive", true));
        request.setPermissionCode("system:user:query");
        request.setLayout("default");
        request.setRoleCodes(List.of("ROLE_SUPER_ADMIN"));
        request.setSort(1);
        request.setVisible(1);
        request.setKeepAlive(1);
        request.setAlwaysShow(0);
        request.setStatus(0);
        return request;
    }
}
