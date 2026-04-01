package com.luckycolor.admin.modules.system.dictionary.type.web;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.dictionary.type.service.DictionaryTypeService;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypeDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DictionaryTypeControllerTest {

    @Test
    void shouldReturnDictionaryTypePage() throws Exception {
        DictionaryTypeService service = Mockito.mock(DictionaryTypeService.class);
        Mockito.when(service.pageDictionaryTypes(any())).thenReturn(PageResult.of(List.of(
            new DictionaryTypePageResponse(1L, 1L, "user_status", "User Status", 0, 1, "default")
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryTypeController(service)).build();

        mockMvc.perform(get("/admin/dictionary-types/page").param("typeCode", "user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].typeCode").value("user_status"));
    }

    @Test
    void shouldReturnDictionaryTypeDetail() throws Exception {
        DictionaryTypeService service = Mockito.mock(DictionaryTypeService.class);
        Mockito.when(service.getDictionaryType(1L)).thenReturn(
            new DictionaryTypeDetailResponse(1L, 1L, "user_status", "User Status", 0, 1, "default")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryTypeController(service)).build();

        mockMvc.perform(get("/admin/dictionary-types/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.typeName").value("User Status"));
    }

    @Test
    void shouldCreateDictionaryType() throws Exception {
        DictionaryTypeService service = Mockito.mock(DictionaryTypeService.class);
        Mockito.when(service.createDictionaryType(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryTypeController(service)).build();

        mockMvc.perform(post("/admin/dictionary-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"typeCode":"user_status","typeName":"User Status","status":0,"sort":1,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldUpdateDictionaryType() throws Exception {
        DictionaryTypeService service = Mockito.mock(DictionaryTypeService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryTypeController(service)).build();

        mockMvc.perform(put("/admin/dictionary-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"typeCode":"user_status","typeName":"User Status","status":0,"sort":1,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldDeleteDictionaryType() throws Exception {
        DictionaryTypeService service = Mockito.mock(DictionaryTypeService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryTypeController(service)).build();

        mockMvc.perform(delete("/admin/dictionary-types/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }
}
