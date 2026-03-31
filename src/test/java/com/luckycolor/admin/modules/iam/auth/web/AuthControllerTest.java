package com.luckycolor.admin.modules.iam.auth.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.luckycolor.admin.modules.iam.auth.service.LoginCaptchaService;
import com.luckycolor.admin.modules.iam.auth.web.response.LoginCaptchaResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    @Test
    void shouldReturnCaptcha() throws Exception {
        LoginCaptchaService loginCaptchaService = Mockito.mock(LoginCaptchaService.class);
        when(loginCaptchaService.createCaptcha()).thenReturn(
            new LoginCaptchaResponse("captcha-1", "data:image/svg+xml;base64,abc")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(loginCaptchaService)).build();

        mockMvc.perform(get("/auth/captcha"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.captchaKey").value("captcha-1"))
            .andExpect(jsonPath("$.data.captchaImage").value("data:image/svg+xml;base64,abc"));
    }
}
