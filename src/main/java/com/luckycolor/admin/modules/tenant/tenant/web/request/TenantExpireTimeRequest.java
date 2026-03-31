package com.luckycolor.admin.modules.tenant.tenant.web.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantExpireTimeRequest {

    @NotNull
    private LocalDateTime expireTime;
}
