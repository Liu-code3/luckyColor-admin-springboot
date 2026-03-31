package com.luckycolor.admin.common.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    @Test
    void shouldHandleValidationException() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ApiErrorCode.BAD_REQUEST))
            .andExpect(jsonPath("$.message").value("name is required"));
    }

    @Test
    void shouldHandleResponseStatusException() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(get("/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ApiErrorCode.NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Tenant not found"));
    }

    @Test
    void shouldHandleUnexpectedException() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(get("/test/error"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(ApiErrorCode.INTERNAL_SERVER_ERROR))
            .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    private MockMvc buildMockMvc() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @RestController
    @Validated
    static class TestController {

        @PostMapping("/test/validation")
        String validate(@Valid @RequestBody TestRequest request) {
            return "ok";
        }

        @GetMapping("/test/not-found")
        String notFound() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found");
        }

        @GetMapping("/test/error")
        String error() {
            throw new IllegalStateException("boom");
        }
    }

    @Getter
    @Setter
    static class TestRequest {

        @NotBlank(message = "name is required")
        private String name;
    }
}
