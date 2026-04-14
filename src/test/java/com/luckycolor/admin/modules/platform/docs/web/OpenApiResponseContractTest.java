package com.luckycolor.admin.modules.platform.docs.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.common.config.OpenApiConfig;
import com.luckycolor.admin.infrastructure.security.config.SecurityJwtProperties;
import com.luckycolor.admin.modules.iam.auth.config.LoginCaptchaProperties;
import com.luckycolor.admin.modules.iam.auth.service.AuthAntiAbuseService;
import com.luckycolor.admin.modules.iam.auth.service.AuthService;
import com.luckycolor.admin.modules.iam.auth.service.LegacyLoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.AuthController;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.web.StorageController;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.SystemConfigController;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.SystemUserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = OpenApiResponseContractTest.TestApplication.class,
    properties = "app.persistence.mapper-scan-enabled=true"
)
@AutoConfigureMockMvc
class OpenApiResponseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemUserService systemUserService;

    @MockBean
    private SystemConfigService systemConfigService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private AuthService authService;

    @MockBean
    private LoginCaptchaService loginCaptchaService;

    @MockBean
    private LegacyLoginCaptchaService legacyLoginCaptchaService;

    @MockBean
    private LoginCaptchaProperties loginCaptchaProperties;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private SecurityJwtProperties securityJwtProperties;

    @MockBean
    private AuthAntiAbuseService authAntiAbuseService;

    @Test
    void shouldExposeErrorExamplesAndBinaryMediaTypesInOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("用户名已存在")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("系统参数键已存在")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("未登录或登录已失效")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("无权限访问")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("文件不存在")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("application/octet-stream")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("text/csv")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("\"format\":\"binary\"")));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
    })
    @Import({
        OpenApiConfig.class,
        AuthController.class,
        StorageController.class,
        SystemUserController.class,
        SystemConfigController.class
    })
    static class TestApplication {
    }
}
