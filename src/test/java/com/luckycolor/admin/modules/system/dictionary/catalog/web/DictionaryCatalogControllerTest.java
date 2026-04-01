package com.luckycolor.admin.modules.system.dictionary.catalog.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.system.dictionary.catalog.service.DictionaryCatalogService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DictionaryCatalogControllerTest {

    @Test
    void shouldReturnDictionaryItemsByType() throws Exception {
        DictionaryCatalogService service = Mockito.mock(DictionaryCatalogService.class);
        Mockito.when(service.listItemsByType("user_status")).thenReturn(List.of(
            new DictionaryCatalogItemResponse(1L, 0L, "Enabled", "0", "success", List.of())
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryCatalogController(service)).build();

        mockMvc.perform(get("/admin/dictionaries/user_status/items"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].label").value("Enabled"));
    }

    @Test
    void shouldReturnDictionaryCatalog() throws Exception {
        DictionaryCatalogService service = Mockito.mock(DictionaryCatalogService.class);
        Mockito.when(service.listCatalog(List.of("user_status"))).thenReturn(List.of(
            new DictionaryCatalogResponse(
                "user_status",
                "User Status",
                List.of(new DictionaryCatalogItemResponse(1L, 0L, "Enabled", "0", "success", List.of()))
            )
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictionaryCatalogController(service)).build();

        mockMvc.perform(get("/admin/dictionaries/catalog").param("typeCodes", "user_status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].typeCode").value("user_status"))
            .andExpect(jsonPath("$.data[0].items[0].label").value("Enabled"));
    }
}
