package com.luckycolor.admin.modules.iam.auth.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthLoginRequest {

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;

    @Size(max = 64)
    private String captchaKey;

    @Size(max = 20)
    private String captchaCode;

    private String remoteIp;
}
