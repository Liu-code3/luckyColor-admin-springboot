package com.luckycolor.admin.modules.system.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.impl.MenuServiceImpl;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class MenuServiceImplTest {

    @Test
    void shouldReturnMenuTreeSortedByParentAndSort() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
            menu(2L, 1L, "System Role", 2),
            menu(1L, 0L, "System", 1),
            menu(3L, 1L, "System User", 1)
        ));
        MenuService service = new MenuServiceImpl(mapper);

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
        MenuService service = new MenuServiceImpl(mapper);

        MenuDetailResponse result = service.getMenu(1L);

        assertThat(result.menuName()).isEqualTo("Dashboard");
        assertThat(result.roleCodes()).containsExactly("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
        assertThat(result.remark()).isEqualTo("default");
    }

    @Test
    void shouldThrowWhenMenuNotFound() {
        MenuMapper mapper = Mockito.mock(MenuMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        MenuService service = new MenuServiceImpl(mapper);

        assertThatThrownBy(() -> service.getMenu(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private MenuDO menu(Long id, Long parentId, String menuName, Integer sort) {
        MenuDO menu = new MenuDO();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setMenuType("MENU");
        menu.setRouteName(menuName.replace(" ", ""));
        menu.setRoutePath("/" + menuName.toLowerCase().replace(" ", "-"));
        menu.setComponent("system/" + id);
        menu.setPermissionCode("system:menu:query");
        menu.setSort(sort);
        menu.setVisible(1);
        menu.setKeepAlive(0);
        menu.setAlwaysShow(0);
        menu.setStatus(0);
        return menu;
    }
}
