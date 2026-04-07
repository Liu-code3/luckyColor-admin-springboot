package com.luckycolor.admin.modules.frontend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.page.PageQuery;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.config.dataobject.SystemConfigDO;
import com.luckycolor.admin.modules.system.config.mapper.SystemConfigMapper;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.item.dataobject.DictionaryItemDO;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.item.service.DictionaryItemService;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import com.luckycolor.admin.modules.system.dictionary.type.service.DictionaryTypeService;
import com.luckycolor.admin.modules.system.notice.dataobject.NoticeDO;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import com.luckycolor.admin.modules.system.notice.service.NoticeService;
import com.luckycolor.admin.modules.system.notice.service.request.NoticePublishCommand;
import com.luckycolor.admin.modules.system.notice.service.request.NoticeWriteRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FrontendContentCompatibilityControllerTest {

    private SystemConfigMapper systemConfigMapper;
    private SystemConfigService systemConfigService;
    private DictionaryTypeService dictionaryTypeService;
    private DictionaryTypeMapper dictionaryTypeMapper;
    private DictionaryItemService dictionaryItemService;
    private DictionaryItemMapper dictionaryItemMapper;
    private DictionaryCatalogCacheService dictionaryCatalogCacheService;
    private NoticeMapper noticeMapper;
    private NoticeService noticeService;
    private DataScopeConditionBuilder dataScopeConditionBuilder;
    private FrontendContentCompatibilityController controller;

    @BeforeEach
    void setUp() {
        initializeTableInfo(SystemConfigDO.class);
        initializeTableInfo(DictionaryTypeDO.class);
        initializeTableInfo(DictionaryItemDO.class);
        initializeTableInfo(NoticeDO.class);
        systemConfigMapper = Mockito.mock(SystemConfigMapper.class);
        systemConfigService = Mockito.mock(SystemConfigService.class);
        dictionaryTypeService = Mockito.mock(DictionaryTypeService.class);
        dictionaryTypeMapper = Mockito.mock(DictionaryTypeMapper.class);
        dictionaryItemService = Mockito.mock(DictionaryItemService.class);
        dictionaryItemMapper = Mockito.mock(DictionaryItemMapper.class);
        dictionaryCatalogCacheService = Mockito.mock(DictionaryCatalogCacheService.class);
        noticeMapper = Mockito.mock(NoticeMapper.class);
        noticeService = Mockito.mock(NoticeService.class);
        dataScopeConditionBuilder = Mockito.mock(DataScopeConditionBuilder.class);
        controller = new FrontendContentCompatibilityController(
            systemConfigMapper,
            systemConfigService,
            dictionaryTypeService,
            dictionaryTypeMapper,
            dictionaryItemService,
            dictionaryItemMapper,
            dictionaryCatalogCacheService,
            noticeMapper,
            noticeService,
            dataScopeConditionBuilder,
            new ObjectMapper()
        );
    }

    private void initializeTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
    }

    @Test
    void shouldExposeConfigPagingContract() throws Exception {
        SystemConfigDO config = new SystemConfigDO();
        config.setId(11L);
        config.setTenantId(1L);
        config.setConfigKey("site.title");
        config.setConfigName("Site Title");
        config.setConfigValue("LuckyColor");
        config.setStatus(0);
        config.setCreateTime(LocalDateTime.of(2026, 4, 3, 9, 0));
        config.setUpdateTime(LocalDateTime.of(2026, 4, 3, 10, 0));

        when(systemConfigMapper.selectPageResult(any(PageQuery.class), any())).thenReturn(PageResult.of(List.of(config), 1));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/configs").param("page", "1").param("size", "10").param("keyword", "site"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.current").value(1))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].id").value("11"))
            .andExpect(jsonPath("$.data.records[0].configKey").value("site.title"))
            .andExpect(jsonPath("$.data.records[0].valueType").value("string"))
            .andExpect(jsonPath("$.data.records[0].status").value(true));
    }

    @Test
    void shouldExposeDictionaryTreeWithPrefixedIds() throws Exception {
        DictionaryTypeDO type = new DictionaryTypeDO();
        type.setId(1L);
        type.setTenantId(1L);
        type.setTypeCode("user_status");
        type.setTypeName("User Status");
        type.setStatus(0);
        type.setSort(10);

        DictionaryItemDO item = new DictionaryItemDO();
        item.setId(1L);
        item.setTenantId(1L);
        item.setTypeCode("user_status");
        item.setParentId(0L);
        item.setItemLabel("Enabled");
        item.setItemValue("ENABLE");
        item.setStatus(0);
        item.setSort(20);

        when(dictionaryTypeMapper.selectList(any())).thenReturn(List.of(type));
        when(dictionaryItemMapper.selectList(any())).thenReturn(List.of(item));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/dict/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value("type:1"))
            .andExpect(jsonPath("$.data[0].children[0].id").value("item:1"))
            .andExpect(jsonPath("$.data[0].children[0].parentId").value("type:1"));
    }

    @Test
    void shouldUseTypePrefixedParentWhenCreatingDictionaryItem() {
        DictionaryTypeDO type = new DictionaryTypeDO();
        type.setId(1L);
        type.setTenantId(1L);
        type.setTypeCode("user_status");
        type.setTypeName("User Status");
        type.setStatus(0);
        type.setSort(10);

        DictionaryItemDO created = new DictionaryItemDO();
        created.setId(9L);
        created.setTenantId(1L);
        created.setTypeCode("user_status");
        created.setParentId(0L);
        created.setItemLabel("Enabled");
        created.setItemValue("ENABLE");
        created.setStatus(0);
        created.setSort(30);

        when(dictionaryTypeMapper.selectById(1L)).thenReturn(type);
        when(dictionaryItemService.createDictionaryItem(any())).thenReturn(9L);
        when(dictionaryItemMapper.selectById(9L)).thenReturn(created);
        when(dictionaryTypeMapper.selectList(any())).thenReturn(List.of(type));

        FrontendContentCompatibilityController.FrontendDictionaryUpsertRequest request =
            new FrontendContentCompatibilityController.FrontendDictionaryUpsertRequest();
        request.setParentId("type:1");
        request.setName("Enabled");
        request.setDictLabel("Enabled");
        request.setDictValue("ENABLE");
        request.setCategory("BIZ");
        request.setSortCode(30);
        request.setWeight(30);
        request.setStatus(true);
        request.setDeleteFlag("NOT_DELETE");

        FrontendContentCompatibilityController.FrontendDictionaryRecord response =
            controller.createDictionary(request).data();

        ArgumentCaptor<DictionaryItemSaveRequest> captor = ArgumentCaptor.forClass(DictionaryItemSaveRequest.class);
        verify(dictionaryItemService).createDictionaryItem(captor.capture());
        assertThat(captor.getValue().getTypeCode()).isEqualTo("user_status");
        assertThat(captor.getValue().getParentId()).isEqualTo(0L);
        assertThat(response.id()).isEqualTo("item:9");
        assertThat(response.parentId()).isEqualTo("type:1");
    }

    @Test
    void shouldExposeNoticeCompatibilityMetadata() throws Exception {
        NoticeDO notice = new NoticeDO();
        notice.setId(5L);
        notice.setTenantId(1L);
        notice.setNoticeTitle("Release Reminder");
        notice.setNoticeContent("Check permissions before release.");
        notice.setNoticeType("release");
        notice.setPublishStatus(1);
        notice.setPublishTime(LocalDateTime.of(2026, 4, 3, 11, 0));
        notice.setCreateTime(LocalDateTime.of(2026, 4, 3, 10, 0));
        notice.setUpdateTime(LocalDateTime.of(2026, 4, 3, 10, 30));
        notice.setRemark("LC_META:{\"publisher\":\"product-team\",\"pinned\":true,\"legacyRemark\":null}");

        when(noticeMapper.selectList(any())).thenReturn(List.of(notice));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/notices").param("page", "1").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].id").value("5"))
            .andExpect(jsonPath("$.data.records[0].status").value(true))
            .andExpect(jsonPath("$.data.records[0].isPinned").value(true))
            .andExpect(jsonPath("$.data.records[0].publisher").value("product-team"));
    }

    @Test
    void shouldTranslateConfigCreateToSystemConfigService() {
        SystemConfigDO created = new SystemConfigDO();
        created.setId(11L);
        created.setTenantId(1L);
        created.setConfigKey("site.title");
        created.setConfigName("Site Title");
        created.setConfigValue("LuckyColor");
        created.setStatus(0);

        when(systemConfigService.createConfig(any())).thenReturn(11L);
        when(systemConfigMapper.selectOne(any())).thenReturn(created);

        FrontendContentCompatibilityController.FrontendConfigUpsertRequest request =
            new FrontendContentCompatibilityController.FrontendConfigUpsertRequest();
        request.setConfigKey(" site.title ");
        request.setConfigName(" Site Title ");
        request.setConfigValue(" LuckyColor ");
        request.setStatus(true);
        request.setRemark("  portal  ");

        FrontendContentCompatibilityController.FrontendConfigRecord response = controller.createConfig(request).data();

        ArgumentCaptor<SystemConfigSaveRequest> captor = ArgumentCaptor.forClass(SystemConfigSaveRequest.class);
        verify(systemConfigService).createConfig(captor.capture());
        assertThat(captor.getValue().getConfigKey()).isEqualTo("site.title");
        assertThat(captor.getValue().getConfigName()).isEqualTo("Site Title");
        assertThat(captor.getValue().getConfigValue()).isEqualTo("LuckyColor");
        assertThat(captor.getValue().getSensitive()).isEqualTo(0);
        assertThat(captor.getValue().getStatus()).isEqualTo(0);
        assertThat(captor.getValue().getSort()).isEqualTo(10);
        assertThat(captor.getValue().getRemark()).isEqualTo("portal");
        assertThat(response.id()).isEqualTo("11");
    }

    @Test
    void shouldTranslateNoticePublishToNoticeService() {
        NoticeDO current = new NoticeDO();
        current.setId(5L);
        current.setNoticeTitle("Release Reminder");
        current.setNoticeContent("Check permissions before release.");
        current.setNoticeType("release");
        current.setPublishStatus(0);
        current.setRemark("LC_META:{\"publisher\":\"ops\",\"pinned\":true,\"legacyRemark\":null}");

        NoticeDO published = new NoticeDO();
        published.setId(5L);
        published.setTenantId(1L);
        published.setNoticeTitle("Release Reminder");
        published.setNoticeContent("Check permissions before release.");
        published.setNoticeType("release");
        published.setPublishStatus(1);
        published.setRemark("LC_META:{\"publisher\":\"product-team\",\"pinned\":true,\"legacyRemark\":null}");

        when(noticeMapper.selectById(5L)).thenReturn(current, published);

        FrontendContentCompatibilityController.FrontendNoticePublishRequest request =
            new FrontendContentCompatibilityController.FrontendNoticePublishRequest();
        request.setPublisher(" product-team ");
        request.setPublishedAt("2026-04-03T11:00:00Z");

        FrontendContentCompatibilityController.FrontendNoticeRecord response = controller.publishNotice(5L, request).data();

        ArgumentCaptor<NoticePublishCommand> captor = ArgumentCaptor.forClass(NoticePublishCommand.class);
        verify(noticeService).publishNotice(eq(5L), captor.capture());
        assertThat(captor.getValue().getPublishStatus()).isEqualTo(1);
        assertThat(captor.getValue().getPublishTime()).isEqualTo(LocalDateTime.of(2026, 4, 3, 11, 0));
        assertThat(captor.getValue().getRemark()).contains("product-team");
        assertThat(captor.getValue().getRemark()).contains("\"pinned\":true");
        assertThat(response.publisher()).isEqualTo("product-team");
        assertThat(response.isPinned()).isTrue();
    }

    @Test
    void shouldTranslateNoticeUpdateToNoticeService() {
        NoticeDO current = new NoticeDO();
        current.setId(5L);
        current.setNoticeTitle("Release Reminder");
        current.setNoticeContent("Check permissions before release.");
        current.setNoticeType("release");
        current.setPublishStatus(1);
        current.setPublishTime(LocalDateTime.of(2026, 4, 3, 11, 0));
        current.setSort(0);
        current.setRemark("LC_META:{\"publisher\":\"product-team\",\"pinned\":false,\"legacyRemark\":null}");

        NoticeDO updated = new NoticeDO();
        updated.setId(5L);
        updated.setTenantId(1L);
        updated.setNoticeTitle("Release Reminder");
        updated.setNoticeContent("Check permissions before release twice.");
        updated.setNoticeType("release");
        updated.setPublishStatus(1);
        updated.setPublishTime(LocalDateTime.of(2026, 4, 3, 11, 0));
        updated.setRemark("LC_META:{\"publisher\":\"product-team\",\"pinned\":false,\"legacyRemark\":null}");

        when(noticeMapper.selectById(5L)).thenReturn(current, updated);

        FrontendContentCompatibilityController.FrontendNoticePatchRequest request =
            new FrontendContentCompatibilityController.FrontendNoticePatchRequest();
        request.setContent(" Check permissions before release twice. ");

        FrontendContentCompatibilityController.FrontendNoticeRecord response = controller.updateNotice(5L, request).data();

        ArgumentCaptor<NoticeWriteRequest> captor = ArgumentCaptor.forClass(NoticeWriteRequest.class);
        verify(noticeService).updateNotice(eq(5L), captor.capture());
        assertThat(captor.getValue().getNoticeTitle()).isEqualTo("Release Reminder");
        assertThat(captor.getValue().getNoticeContent()).isEqualTo("Check permissions before release twice.");
        assertThat(captor.getValue().getPublishStatus()).isEqualTo(1);
        assertThat(captor.getValue().getPublishTime()).isEqualTo(LocalDateTime.of(2026, 4, 3, 11, 0));
        assertThat(response.content()).isEqualTo("Check permissions before release twice.");
    }
}
