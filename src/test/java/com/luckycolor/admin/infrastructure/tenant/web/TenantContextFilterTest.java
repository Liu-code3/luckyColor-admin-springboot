package com.luckycolor.admin.infrastructure.tenant.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.infrastructure.tenant.core.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = {
        LuckycolorAdminSpringbootApplication.class,
        TenantContextFilterTest.TestTenantControllerConfiguration.class
    },
    properties = "app.tenancy.default-tenant-id=2001"
)
@AutoConfigureMockMvc
class TenantContextFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldResolveTenantIdFromHeader() throws Exception {
        mockMvc.perform(get("/internal/tenant-context").with(user("tester")).header("x-tenant-id", "1001"))
            .andExpect(status().isOk())
            .andExpect(content().string("1001"));
    }

    @Test
    void shouldFallbackToDefaultTenantId() throws Exception {
        mockMvc.perform(get("/internal/tenant-context").with(user("tester")))
            .andExpect(status().isOk())
            .andExpect(content().string("2001"));
    }

    @Test
    void shouldResolveTenantIdFromBearerToken() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiu", 3001L, java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/internal/tenant-context").with(user("tester"))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("3001"));
    }

    @Test
    void shouldAcceptMatchingTenantIdFromHeaderAndBearerToken() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiu", 3001L, java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/internal/tenant-context").with(user("tester"))
                .header("x-tenant-id", "3001")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("3001"));
    }

    @Test
    void shouldAcceptMatchingExternalTenantIdFromHeaderAndBearerToken() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiu", 3001L, java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/internal/tenant-context").with(user("tester"))
                .header("x-tenant-id", "tenant_3001")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("3001"));
    }

    @Test
    void shouldRejectMismatchedTenantIdBetweenHeaderAndBearerToken() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiu", 3001L, java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/internal/tenant-context").with(user("tester"))
                .header("x-tenant-id", "1001")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Tenant id header does not match authenticated tenant"));
    }

    @Test
    void shouldPreferBearerTokenTenantWhenHeaderMissing() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiu", 4001L, java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/internal/tenant-context").with(user("tester"))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("4001"));
    }

    @TestConfiguration
    static class TestTenantControllerConfiguration {

        @Bean
        TestTenantController testTenantController() {
            return new TestTenantController();
        }
    }

    @RestController
    static class TestTenantController {

        @GetMapping("/internal/tenant-context")
        String currentTenantId() {
            return String.valueOf(TenantContextHolder.getRequiredTenantId());
        }
    }
}
