package com.luckycolor.admin.modules.tenant.bootstrap.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantBootstrapExecuteRequest {

    @NotBlank
    @Size(max = 100)
    private String templateCode;

    @Size(max = 50)
    private String adminUsername;

    @Size(max = 50)
    private String adminNickname;

    @Size(max = 255)
    private String remark;
}
