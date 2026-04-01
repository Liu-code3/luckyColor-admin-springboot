package com.luckycolor.admin.modules.system.dictionary.item.web;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.system.dictionary.item.service.DictionaryItemService;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DictionaryItemControllerTest {

    @Test
    void shouldReturnDictionaryItemTree() throws Exception {
        DictionaryItemService service = Mockito.mock(DictionaryItemService.class);
        Mockito.when(service.listDictionaryItemTree(any())).thenReturn(List.of(
            new DictionaryItemTreeResponse(
                1L,
                1L,
                "user_status",
                0L,
                "Status",
                "root",
                null,
                1,
                0,
                List.of(new DictionaryItemTreeResponse(
                    2L,
                    1L,
                    "user_status",
                    1L,
                    "Enabled",
                    "0",
                    "success",
                    1,
                    0,
                    List.of()
                ))
            )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryItemController(service)).build();

        mockMvc.perform(get("/admin/dictionary-items/tree").param("typeCode", "user_status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].children[0].itemLabel").value("Enabled"));
    }

    @Test
    void shouldReturnDictionaryItemDetail() throws Exception {
        DictionaryItemService service = Mockito.mock(DictionaryItemService.class);
        Mockito.when(service.getDictionaryItem(1L)).thenReturn(
            new DictionaryItemDetailResponse(1L, 1L, "user_status", 0L, "Enabled", "0", "success", 1, 0, "default")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryItemController(service)).build();

        mockMvc.perform(get("/admin/dictionary-items/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.itemLabel").value("Enabled"));
    }

    @Test
    void shouldCreateDictionaryItem() throws Exception {
        DictionaryItemService service = Mockito.mock(DictionaryItemService.class);
        Mockito.when(service.createDictionaryItem(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryItemController(service)).build();

        mockMvc.perform(post("/admin/dictionary-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"typeCode":"user_status","parentId":0,"itemLabel":"Enabled","itemValue":"0","itemTag":"success","sort":1,"status":0,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldDeleteDictionaryItem() throws Exception {
        DictionaryItemService service = Mockito.mock(DictionaryItemService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryItemController(service)).build();

        mockMvc.perform(delete("/admin/dictionary-items/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }
}
