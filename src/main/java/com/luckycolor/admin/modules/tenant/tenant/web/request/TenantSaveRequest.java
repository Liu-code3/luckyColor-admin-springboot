package com.luckycolor.admin.modules.tenant.tenant.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Create or update tenant request")
public class TenantSaveRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Tenant name", example = "LuckyColor Demo")
    private String name;

    @NotNull
    @Schema(description = "Tenant package ID", example = "10")
    private Long packageId;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Contact name", example = "张三")
    private String contactName;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "Contact mobile", example = "13800138000")
    private String contactMobile;

    @NotNull
    @Min(1)
    @Max(100000)
    @Schema(description = "Allowed account count", example = "100")
    private Integer accountCount;

    @NotNull
    @Schema(description = "Expiration time", example = "2026-12-31T23:59:59")
    private LocalDateTime expireTime;

    @NotNull
    @Min(0)
    @Max(1)
    @Schema(description = "Status: 0 enabled, 1 disabled", example = "0")
    private Integer status;
}
