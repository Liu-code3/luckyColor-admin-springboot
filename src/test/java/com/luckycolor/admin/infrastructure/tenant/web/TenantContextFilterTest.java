package com.luckycolor.admin.infrastructure.tenant.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.luckycolor.admin.LuckycolorAdminSpringbootApplication;
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
