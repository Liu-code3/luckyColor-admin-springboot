package com.luckycolor.admin.infrastructure.security.authorization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = {
        LuckycolorAdminSpringbootApplication.class,
        PermissionGuardIntegrationTest.PermissionGuardControllerConfiguration.class
    }
)
@AutoConfigureMockMvc
class PermissionGuardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private AuthUserService authUserService;

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/internal/permission/query").header("x-tenant-id", "1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40100))
            .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void shouldReturnForbiddenWhenPermissionMissing() throws Exception {
        Mockito.when(authUserService.getByUserId(2L)).thenReturn(
            new AuthUser(2L, "viewer", null, 1L, "Viewer", 0, List.of("ROLE_VIEWER"), List.of("system:user:query"))
        );
        String token = jwtTokenService.createAccessToken(2L, "viewer", 1L, List.of("ROLE_VIEWER"));

        mockMvc.perform(get("/internal/permission/query").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40300))
            .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void shouldAllowWhenPermissionGranted() throws Exception {
        Mockito.when(authUserService.getByUserId(1L)).thenReturn(
            new AuthUser(1L, "admin", null, 1L, "Admin", 0, List.of("ROLE_SUPER_ADMIN"), List.of())
        );
        String token = jwtTokenService.createAccessToken(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"));

        mockMvc.perform(get("/internal/permission/query").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("allowed"));
    }

    @Test
    void shouldAllowWhenAnyPermissionMatched() throws Exception {
        Mockito.when(authUserService.getByUserId(3L)).thenReturn(
            new AuthUser(3L, "operator", null, 1L, "Operator", 0, List.of("ROLE_OPERATOR"), List.of("tenant:update"))
        );
        String token = jwtTokenService.createAccessToken(3L, "operator", 1L, List.of("ROLE_OPERATOR"));

        mockMvc.perform(get("/internal/permission/manage").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("managed"));
    }

    @TestConfiguration
    static class PermissionGuardControllerConfiguration {

        @Bean
        PermissionGuardTestController permissionGuardTestController() {
            return new PermissionGuardTestController();
        }
    }

    @RestController
    static class PermissionGuardTestController {

        @GetMapping("/internal/permission/query")
        @RequirePermission("tenant:query")
        String query() {
            return "allowed";
        }

        @GetMapping("/internal/permission/manage")
        @RequireAnyPermission({ "tenant:create", "tenant:update" })
        String manage() {
            return "managed";
        }
    }
}
