package com.luckycolor.admin.modules.frontend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.service.StoredFile;
import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.config.TenantBootstrapProperties;
import com.luckycolor.admin.modules.tenant.bootstrap.mapper.TenantBootstrapRecordMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.packageinfo.dataobject.TenantPackageDO;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.profile.dataobject.TenantProfileDO;
import com.luckycolor.admin.modules.tenant.profile.mapper.TenantProfileMapper;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FrontendTenantCompatibilityControllerTest {

    private TenantService tenantService;
    private TenantMapper tenantMapper;
    private TenantPackageService tenantPackageService;
    private TenantPackageMapper tenantPackageMapper;
    private TenantProfileMapper tenantProfileMapper;
    private TenantBootstrapService tenantBootstrapService;
    private TenantBootstrapRecordMapper tenantBootstrapRecordMapper;
    private TenantAuditLogMapper tenantAuditLogMapper;
    private MenuMapper menuMapper;
    private FileStorageService fileStorageService;
    private DataScopeConditionBuilder dataScopeConditionBuilder;
    private FrontendTenantCompatibilityController controller;

    @BeforeEach
    void setUp() {
        initializeTableInfo(TenantDO.class);
        initializeTableInfo(TenantPackageDO.class);
        initializeTableInfo(TenantProfileDO.class);
        initializeTableInfo(MenuDO.class);
        tenantService = Mockito.mock(TenantService.class);
        tenantMapper = Mockito.mock(TenantMapper.class);
        tenantPackageService = Mockito.mock(TenantPackageService.class);
        tenantPackageMapper = Mockito.mock(TenantPackageMapper.class);
        tenantProfileMapper = Mockito.mock(TenantProfileMapper.class);
        tenantBootstrapService = Mockito.mock(TenantBootstrapService.class);
        tenantBootstrapRecordMapper = Mockito.mock(TenantBootstrapRecordMapper.class);
        tenantAuditLogMapper = Mockito.mock(TenantAuditLogMapper.class);
        menuMapper = Mockito.mock(MenuMapper.class);
        fileStorageService = Mockito.mock(FileStorageService.class);
        dataScopeConditionBuilder = Mockito.mock(DataScopeConditionBuilder.class);

        TenantBootstrapProperties bootstrapProperties = new TenantBootstrapProperties();
        TenantBootstrapProperties.Template template = new TenantBootstrapProperties.Template();
        template.setCode("default");
        template.setRoleCodes(List.of("tenant_admin", "tenant_member"));
        template.setMenuCodes(List.of("dashboard", "system:user"));
        bootstrapProperties.setTemplates(List.of(template));

        controller = new FrontendTenantCompatibilityController(
            tenantService,
            tenantMapper,
            tenantPackageService,
            tenantPackageMapper,
            tenantProfileMapper,
            tenantBootstrapService,
            bootstrapProperties,
            tenantBootstrapRecordMapper,
            tenantAuditLogMapper,
            menuMapper,
            fileStorageService,
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
    void shouldExposeTenantPagingContract() throws Exception {
        TenantDO tenant = new TenantDO();
        tenant.setId(7L);
        tenant.setName("Acme");
        tenant.setPackageId(9L);
        tenant.setContactName("Alice");
        tenant.setContactMobile("13800138000");
        tenant.setExpireTime(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        tenant.setStatus(0);
        tenant.setCreateTime(LocalDateTime.of(2026, 4, 3, 9, 0));
        tenant.setUpdateTime(LocalDateTime.of(2026, 4, 3, 10, 0));

        TenantProfileDO profile = new TenantProfileDO();
        profile.setId(7L);
        profile.setTenantCode("acme");
        profile.setStatusCode("FROZEN");
        profile.setContactEmail("admin@acme.local");
        profile.setRemark("compat profile");

        TenantPackageDO tenantPackage = new TenantPackageDO();
        tenantPackage.setId(9L);
        tenantPackage.setPackageName("Professional");
        tenantPackage.setStatus(0);
        tenantPackage.setRemark("LC_META:{\"code\":\"pro\",\"legacyRemark\":\"frontend package\"}");

        when(tenantMapper.selectList(any())).thenReturn(List.of(tenant));
        when(tenantProfileMapper.selectBatchIds(List.of(7L))).thenReturn(List.of(profile));
        when(tenantPackageMapper.selectBatchIds(List.of(9L))).thenReturn(List.of(tenantPackage));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/tenants").param("page", "1").param("size", "10").param("keyword", "acme"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].id").value("7"))
            .andExpect(jsonPath("$.data.records[0].code").value("acme"))
            .andExpect(jsonPath("$.data.records[0].status").value("FROZEN"))
            .andExpect(jsonPath("$.data.records[0].tenantPackage.code").value("pro"))
            .andExpect(jsonPath("$.data.records[0].contactEmail").value("admin@acme.local"));
    }

    @Test
    void shouldExposeTenantPackageMenuAssignment() {
        TenantPackageDO tenantPackage = new TenantPackageDO();
        tenantPackage.setId(11L);
        tenantPackage.setPackageName("Starter");
        tenantPackage.setRemark("LC_META:{\"code\":\"starter\",\"menuIds\":[1,11,12]}");

        when(tenantPackageMapper.selectById(11L)).thenReturn(tenantPackage);

        FrontendTenantCompatibilityController.FrontendTenantPackageMenuAssignment response =
            controller.getTenantPackageMenus(11L).data();

        assertThat(response.menuIds()).containsExactly(1L, 11L, 12L);
    }

    @Test
    void shouldExposeFrontendFileUploadContract() throws Exception {
        when(fileStorageService.upload(any())).thenReturn(
            new FileUploadResponse(
                "avatar.png",
                "2026/04/03/avatar.png",
                "2026/04/03/avatar.png",
                12L,
                "image/png",
                "/api/admin/files/download?path=2026/04/03/avatar.png"
            )
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(multipart("/file/upload").file(new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1, 2, 3})))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("avatar.png"))
            .andExpect(jsonPath("$.data.url").value("/api/file/2026/04/03/avatar.png"));
    }

    @Test
    void shouldNormalizeFrontendReadUrlWhenDeletingFile() {
        when(fileStorageService.delete("2026/04/03/avatar.png")).thenReturn(true);

        Boolean deleted = controller.deleteFile("http://127.0.0.1:3001/api/file/2026/04/03/avatar.png").data();

        assertThat(deleted).isTrue();
        verify(fileStorageService).delete("2026/04/03/avatar.png");
    }

    @Test
    void shouldServeFrontendFileReadEndpointWithoutPermissionHeader() throws Exception {
        when(fileStorageService.download("2026/04/03/avatar.png")).thenReturn(
            new StoredFile(new ByteArrayResource(new byte[] {1, 2, 3}), "avatar.png", 3L, "image/png")
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/file/2026/04/03/avatar.png"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andExpect(content().bytes(new byte[] {1, 2, 3}));
    }

    @Test
    void shouldReturnStableDictionaryIdsWhenCreatingTenant() {
        TenantPackageDO tenantPackage = new TenantPackageDO();
        tenantPackage.setId(9L);
        tenantPackage.setPackageName("Default Package");
        tenantPackage.setStatus(0);
        tenantPackage.setRemark("LC_META:{\"code\":\"default_package\",\"menuIds\":[1,11]}");

        TenantDO createdTenant = new TenantDO();
        createdTenant.setId(21L);
        createdTenant.setName("Acme Cloud");
        createdTenant.setPackageId(9L);
        createdTenant.setContactName("Alice");
        createdTenant.setContactMobile("13800138000");
        createdTenant.setExpireTime(LocalDateTime.of(2027, 4, 3, 9, 0));
        createdTenant.setStatus(0);
        createdTenant.setCreateTime(LocalDateTime.of(2026, 4, 3, 9, 0));
        createdTenant.setUpdateTime(LocalDateTime.of(2026, 4, 3, 9, 5));

        TenantProfileDO savedProfile = new TenantProfileDO();
        savedProfile.setId(21L);
        savedProfile.setTenantCode("tenant_21");
        savedProfile.setStatusCode("ACTIVE");
        savedProfile.setAdminUsername("acme_admin");
        savedProfile.setAdminNickname("Acme Admin");
        savedProfile.setContactEmail("admin@acme.local");

        MenuDO dashboardMenu = new MenuDO();
        dashboardMenu.setId(1L);
        dashboardMenu.setRoutePath("/dashboard");

        MenuDO userMenu = new MenuDO();
        userMenu.setId(11L);
        userMenu.setPermissionCode("system:user:query");

        when(tenantProfileMapper.selectOne(any())).thenReturn(null);
        when(tenantPackageMapper.selectList(any())).thenReturn(List.of(tenantPackage));
        when(tenantService.createTenant(any())).thenReturn(21L);
        when(tenantMapper.selectById(21L)).thenReturn(createdTenant);
        when(tenantProfileMapper.selectById(21L)).thenReturn(null, savedProfile, savedProfile);
        when(tenantPackageMapper.selectById(9L)).thenReturn(tenantPackage, tenantPackage);
        when(menuMapper.selectList(any())).thenReturn(List.of(dashboardMenu, userMenu));

        FrontendTenantCompatibilityController.FrontendTenantCreateRequest request =
            new FrontendTenantCompatibilityController.FrontendTenantCreateRequest();
        request.setCode("tenant_21");
        request.setName("Acme Cloud");
        request.setAdminPassword("123456");
        request.setAdminUsername("acme_admin");
        request.setAdminNickname("Acme Admin");
        request.setContactEmail("admin@acme.local");

        FrontendTenantCompatibilityController.FrontendTenantInitResult result = controller.createTenant(request).data();

        assertThat(result.menuIds()).containsExactly(1L, 11L);
        assertThat(result.dictionaryIds()).containsExactly(
            "tenant_21_notice_scope_root",
            "tenant_21_notice_scope_all",
            "tenant_21_notice_scope_department",
            "tenant_21_notice_scope_role"
        );
    }
}
