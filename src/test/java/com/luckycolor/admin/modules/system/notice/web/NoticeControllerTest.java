package com.luckycolor.admin.modules.system.notice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.notice.service.NoticeService;
import com.luckycolor.admin.modules.system.notice.web.response.NoticeDetailResponse;
import com.luckycolor.admin.modules.system.notice.web.response.NoticePageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NoticeControllerTest {

    @Test
    void shouldReturnNoticePage() throws Exception {
        NoticeService service = Mockito.mock(NoticeService.class);
        Mockito.when(service.pageNotices(any())).thenReturn(PageResult.of(List.of(
            new NoticePageResponse(1L, 1L, "Platform Notice", "SYSTEM", 1, LocalDateTime.of(2026, 4, 1, 10, 0), 1, "default")
        ), 1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NoticeController(service)).build();

        mockMvc.perform(get("/admin/notices/page").param("noticeType", "SYSTEM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].noticeTitle").value("Platform Notice"));
    }

    @Test
    void shouldReturnNoticeDetail() throws Exception {
        NoticeService service = Mockito.mock(NoticeService.class);
        Mockito.when(service.getNotice(1L)).thenReturn(
            new NoticeDetailResponse(1L, 1L, "Platform Notice", "SYSTEM", "system maintenance", 1, LocalDateTime.of(2026, 4, 1, 10, 0), 1, "default")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NoticeController(service)).build();

        mockMvc.perform(get("/admin/notices/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.noticeContent").value("system maintenance"));
    }

    @Test
    void shouldCreateNotice() throws Exception {
        NoticeService service = Mockito.mock(NoticeService.class);
        Mockito.when(service.createNotice(any())).thenReturn(1L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NoticeController(service)).build();

        mockMvc.perform(post("/admin/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"noticeTitle":"Platform Notice","noticeType":"SYSTEM","noticeContent":"system maintenance","sort":1,"remark":"default"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldPublishNotice() throws Exception {
        NoticeService service = Mockito.mock(NoticeService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NoticeController(service)).build();

        mockMvc.perform(put("/admin/notices/1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"publishStatus":1}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(service).publishNotice(eq(1L), any());
    }
}
