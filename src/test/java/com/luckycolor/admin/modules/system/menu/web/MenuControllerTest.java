package com.luckycolor.admin.modules.system.menu.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MenuControllerTest {

    @Test
    void shouldReturnMenuTree() throws Exception {
        MenuService service = Mockito.mock(MenuService.class);
        when(service.listMenuTree(any())).thenReturn(List.of(
            new MenuTreeResponse(
                1L,
                0L,
                "System",
                "DIRECTORY",
                "System",
                "/system",
                "Layout",
                null,
                List.of(),
                "setting",
                1,
                1,
                0,
                1,
                0,
                List.of(new MenuTreeResponse(
                    2L,
                    1L,
                    "System User",
                    "MENU",
                    "SystemUser",
                    "users",
                    "system/user/index",
                    "system:user:query",
                    List.of("ROLE_SUPER_ADMIN"),
                    null,
                    1,
                    1,
                    1,
                    0,
                    0,
                    List.of()
                ))
            )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(service)).build();

        mockMvc.perform(get("/admin/menus/tree").param("menuName", "System"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].menuName").value("System"))
            .andExpect(jsonPath("$.data[0].children[0].menuName").value("System User"));
    }

    @Test
    void shouldReturnMenuDetail() throws Exception {
        MenuService service = Mockito.mock(MenuService.class);
        when(service.getMenu(1L)).thenReturn(new MenuDetailResponse(
            1L,
            0L,
            "Dashboard",
            "MENU",
            "Dashboard",
            "/dashboard",
            "dashboard/index",
            "dashboard:view",
            List.of("ROLE_SUPER_ADMIN"),
            "dashboard",
            1,
            1,
            1,
            0,
            0,
            "default"
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(service)).build();

        mockMvc.perform(get("/admin/menus/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.menuName").value("Dashboard"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("ROLE_SUPER_ADMIN"));
    }
}
