package com.luckycolor.admin.infrastructure.tenant.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
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
        TenantContextFilterMissingTenantTest.TestTenantControllerConfiguration.class
    },
    properties = "app.tenancy.default-tenant-id="
)
@AutoConfigureMockMvc
class TenantContextFilterMissingTenantTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectRequestWithoutTenantId() throws Exception {
        mockMvc.perform(get("/internal/tenant-required").with(user("tester")))
            .andExpect(status().isBadRequest());
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

        @GetMapping("/internal/tenant-required")
        String currentTenantId() {
            return "ok";
        }
    }
}
