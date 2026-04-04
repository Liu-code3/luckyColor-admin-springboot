package com.luckycolor.admin.regression;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.SystemConfigController;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.DictionaryCatalogController;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    classes = {
        LuckycolorAdminSpringbootApplication.class,
        CoreRegressionIntegrationTest.RegressionControllerConfiguration.class
    },
    properties = {
        "app.login-captcha.enabled=false",
        "app.security.auth.local-users[0].user-id=1",
        "app.security.auth.local-users[0].username=admin",
        "app.security.auth.local-users[0].password=admin123",
        "app.security.auth.local-users[0].tenant-id=1",
        "app.security.auth.local-users[0].nickname=System Admin",
        "app.security.auth.local-users[0].status=0",
        "app.security.auth.local-users[0].roles[0]=ROLE_SUPER_ADMIN",
        "app.security.auth.local-users[0].permissions[0]=system:dictionary:query",
        "app.security.auth.local-users[0].permissions[1]=system:config:query"
    }
)
@AutoConfigureMockMvc
class CoreRegressionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryCatalogCacheService dictionaryCatalogCacheService;

    @MockBean
    private SystemConfigService systemConfigService;

    @Test
    void shouldReturnHealthStatusWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    void shouldReturnVersionInfoWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.version").value("1.0.0-alpha.1"))
            .andExpect(jsonPath("$.data.releaseStage").value("alpha"));
    }

    @Test
    void shouldLoginAndUseTokenToReadProfile() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/auth/profile").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.tenantId").value("tenant_001"))
            .andExpect(jsonPath("$.data.roles[0]").value("ROLE_SUPER_ADMIN"));
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() throws Exception {
        mockMvc.perform(post("/auth/login")
                .header("x-tenant-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"wrong-password"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(1011001))
            .andExpect(jsonPath("$.message").value("username or password is incorrect"));
    }

    @Test
    void shouldAccessDictionaryApiAfterLogin() throws Exception {
        when(dictionaryCatalogCacheService.listItemsByType("user_status")).thenReturn(List.of(
            new DictionaryCatalogItemResponse(1L, 0L, "Enabled", "0", "success", List.of())
        ));
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/admin/dictionaries/user_status/items")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].label").value("Enabled"))
            .andExpect(jsonPath("$.data[0].value").value("0"));
    }

    @Test
    void shouldRequireAuthenticationForDictionaryApi() throws Exception {
        mockMvc.perform(get("/admin/dictionaries/user_status/items")
                .header("x-tenant-id", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40100))
            .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void shouldAccessSystemConfigPageAfterLogin() throws Exception {
        when(systemConfigService.pageConfigs(any())).thenReturn(PageResult.of(List.of(
            new SystemConfigPageResponse(1L, 1L, "sms.secret", "SMS Secret", "******", 1, 0, 1, "default")
        ), 1L));
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/admin/system-configs/page")
                .header("Authorization", "Bearer " + token)
                .param("configKey", "sms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].configKey").value("sms.secret"));
    }

    @Test
    void shouldRejectSystemConfigPageWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/system-configs/page")
                .header("x-tenant-id", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40100))
            .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/auth/login")
                .header("x-tenant-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"%s","password":"%s"}
                    """.formatted(username, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return jsonNode.path("data").path("accessToken").asText();
    }

    @TestConfiguration
    static class RegressionControllerConfiguration {

        @Bean
        SystemConfigController systemConfigController(SystemConfigService systemConfigService) {
            return new SystemConfigController(systemConfigService);
        }

        @Bean
        DictionaryCatalogController dictionaryCatalogController(
            DictionaryCatalogCacheService dictionaryCatalogCacheService
        ) {
            return new DictionaryCatalogController(dictionaryCatalogCacheService);
        }
    }
}
