package com.luckycolor.admin.modules.iam.auth.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Username and password login request")
public class AuthLoginRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Login username", example = "admin")
    private String username;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Login password", example = "123456")
    private String password;

    @Size(max = 64)
    @Schema(description = "Captcha key returned by captcha API", example = "captcha-20260402-001")
    private String captchaKey;

    @Size(max = 20)
    @Schema(description = "Captcha code entered by user", example = "7K9P")
    private String captchaCode;

    @Size(max = 128)
    @Schema(description = "One-time captcha token returned by legacy captcha verify API", example = "captcha-pass-token-demo")
    private String captchaToken;

    @Schema(description = "Client IP, populated by server", example = "127.0.0.1")
    private String remoteIp;

    @Schema(description = "Tenant identifier from request header, populated by server", example = "tenant_001")
    private String tenantId;
}
