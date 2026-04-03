package com.luckycolor.admin.modules.system.menu.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
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
                "main_system",
                "Layout",
                null,
                java.util.Map.of("title", "System"),
                null,
                List.of(),
                "setting",
                "default",
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
                    "main_system_users",
                    "system/user/index",
                    null,
                    java.util.Map.of("title", "System User"),
                    "system:user:query",
                    List.of("ROLE_SUPER_ADMIN"),
                    null,
                    "default",
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
            .andExpect(jsonPath("$.code").value(200))
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
            "dashboard",
            "dashboard/index",
            null,
            java.util.Map.of("title", "Dashboard"),
            "dashboard:view",
            List.of("ROLE_SUPER_ADMIN"),
            "dashboard",
            "default",
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
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.menuName").value("Dashboard"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("ROLE_SUPER_ADMIN"));
    }

    @Test
    void shouldCreateMenu() throws Exception {
        MenuService service = Mockito.mock(MenuService.class);
        when(service.createMenu(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(service)).build();

        mockMvc.perform(post("/admin/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"parentId":0,"menuName":"System User","menuType":"MENU","routeName":"SystemUser","routePath":"users","menuKey":"main_system_users","component":"system/user/index","redirect":"/system/users/list","meta":{"title":"System User","keepAlive":true},"permissionCode":"system:user:query","roleCodes":["ROLE_SUPER_ADMIN"],"layout":"default","sort":1,"visible":1,"keepAlive":1,"alwaysShow":0,"status":0}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateMenuStatus() throws Exception {
        MenuService service = Mockito.mock(MenuService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(service)).build();

        mockMvc.perform(put("/admin/menus/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).updateMenuStatus(eq(1L), any());
    }

    @Test
    void shouldDeleteMenu() throws Exception {
        MenuService service = Mockito.mock(MenuService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(service)).build();

        mockMvc.perform(delete("/admin/menus/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).deleteMenu(1L);
    }
}
