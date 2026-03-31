package com.luckycolor.admin.infrastructure.security.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.service.AuthTokenSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = {
        LuckycolorAdminSpringbootApplication.class,
        JwtAuthenticationFilterTest.TestAuthenticationControllerConfiguration.class
    }
)
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AuthTokenSessionService authTokenSessionService;

    @Test
    void shouldAuthenticateRequestFromBearerToken() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiuActive", 1001L, java.util.List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/internal/auth-context").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("coderLiuActive@1001"));
    }

    @Test
    void shouldRejectRevokedToken() throws Exception {
        String token = jwtTokenService.createAccessToken(1L, "coderLiuRevoked", 1001L, java.util.List.of("ROLE_ADMIN"));
        authTokenSessionService.revoke(token, jwtTokenService.resolveExpiration(token));

        mockMvc.perform(get("/internal/auth-context").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(content().json("""
                {"code":40100,"message":"Unauthorized"}
                """, false));
    }

    @TestConfiguration
    static class TestAuthenticationControllerConfiguration {

        @Bean
        TestAuthenticationController testAuthenticationController() {
            return new TestAuthenticationController();
        }
    }

    @RestController
    static class TestAuthenticationController {

        @GetMapping("/internal/auth-context")
        String currentPrincipal(Authentication authentication) {
            JwtAuthenticatedUser principal = (JwtAuthenticatedUser) authentication.getPrincipal();
            return principal.username() + "@" + principal.tenantId();
        }
    }
}
