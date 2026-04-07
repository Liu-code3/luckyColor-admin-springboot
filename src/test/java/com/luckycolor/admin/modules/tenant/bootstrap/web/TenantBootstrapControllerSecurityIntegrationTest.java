package com.luckycolor.admin.modules.tenant.bootstrap.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import java.time.LocalDateTime;
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

@SpringBootTest(
    classes = {
        LuckycolorAdminSpringbootApplication.class,
        TenantBootstrapControllerSecurityIntegrationTest.TenantBootstrapControllerConfiguration.class
    }
)
@AutoConfigureMockMvc
class TenantBootstrapControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private TenantBootstrapService tenantBootstrapService;

    @MockBean
    private AuthUserService authUserService;

    @TestConfiguration
    static class TenantBootstrapControllerConfiguration {

        @Bean
        TenantBootstrapController tenantBootstrapController(TenantBootstrapService tenantBootstrapService) {
            return new TenantBootstrapController(tenantBootstrapService);
        }
    }

    @Test
    void shouldRejectAnonymousTemplateRequest() throws Exception {
        mockMvc.perform(get("/admin/tenant-bootstrap/templates").header("x-tenant-id", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40100))
            .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void shouldRejectTemplateRequestWhenQueryPermissionMissing() throws Exception {
        String token = stubUser(2L, "viewer", List.of("ROLE_VIEWER"), List.of("tenant:query"));

        mockMvc.perform(get("/admin/tenant-bootstrap/templates")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40300))
            .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void shouldRejectRecordPageRequestWhenQueryPermissionMissing() throws Exception {
        String token = stubUser(2L, "viewer", List.of("ROLE_VIEWER"), List.of("tenant:query"));

        mockMvc.perform(get("/admin/tenant-bootstrap/records/page")
                .param("tenantId", "1")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40300))
            .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void shouldAllowRecordPageRequestForSuperAdmin() throws Exception {
        when(tenantBootstrapService.pageRecords(any())).thenReturn(PageResult.of(
            List.of(recordResponse()),
            1L
        ));
        String token = stubUser(1L, "admin", List.of("ROLE_SUPER_ADMIN"), List.of("tenant:bootstrap:query"));

        mockMvc.perform(get("/admin/tenant-bootstrap/records/page")
                .param("tenantId", "1")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void shouldRejectBootstrapRequestWhenExecutePermissionMissing() throws Exception {
        String token = stubUser(2L, "viewer", List.of("ROLE_VIEWER"), List.of("tenant:query"));

        mockMvc.perform(post("/admin/tenants/1/bootstrap")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"templateCode":"default","adminUsername":"admin"}
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40300))
            .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void shouldAllowBootstrapRequestForSuperAdmin() throws Exception {
        when(tenantBootstrapService.bootstrapTenant(eq(1L), any())).thenReturn(recordResponse());
        String token = stubUser(1L, "admin", List.of("ROLE_SUPER_ADMIN"), List.of("tenant:bootstrap:execute"));

        mockMvc.perform(post("/admin/tenants/1/bootstrap")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"templateCode":"default","adminUsername":"admin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.templateCode").value("default"));
    }

    private String stubUser(Long userId, String username, List<String> roles, List<String> permissions) {
        when(authUserService.getByUserId(userId)).thenReturn(
            new AuthUser(userId, username, null, 1L, username, 0, roles, permissions)
        );
        return jwtTokenService.createAccessToken(userId, username, 1L, roles);
    }

    private TenantBootstrapRecordResponse recordResponse() {
        return new TenantBootstrapRecordResponse(
            2L,
            1L,
            "default",
            "默认租户模板",
            List.of("tenant_admin"),
            List.of("dashboard"),
            "admin",
            "租户管理员",
            1,
            LocalDateTime.of(2026, 3, 31, 12, 0),
            "首次初始化"
        );
    }
}
