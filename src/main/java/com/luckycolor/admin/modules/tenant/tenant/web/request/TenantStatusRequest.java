package com.luckycolor.admin.modules.tenant.tenant.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantStatusRequest {

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
