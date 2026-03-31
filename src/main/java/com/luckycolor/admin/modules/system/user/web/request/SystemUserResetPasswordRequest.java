package com.luckycolor.admin.modules.system.user.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemUserResetPasswordRequest {

    @NotBlank
    private String newPassword;
}
