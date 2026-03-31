package com.luckycolor.admin.modules.tenant.tenant.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantSaveRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Long packageId;

    @NotBlank
    @Size(max = 50)
    private String contactName;

    @NotBlank
    @Size(max = 20)
    private String contactMobile;

    @NotNull
    @Min(1)
    @Max(100000)
    private Integer accountCount;

    @NotNull
    private LocalDateTime expireTime;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
