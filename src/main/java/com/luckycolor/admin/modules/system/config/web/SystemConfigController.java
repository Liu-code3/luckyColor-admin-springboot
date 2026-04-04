package com.luckycolor.admin.modules.system.config.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.SYSTEM_CONFIG_KEY_ALREADY_EXISTS;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.config.mapper.SystemConfigMapper;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigPageQuery;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigSaveRequest;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigDetailResponse;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/system-configs")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "System Configs", description = "System configuration management APIs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/page")
    @RequirePermission("system:config:query")
    @Operation(summary = "Page system configs")
    public ApiResponse<PageResult<SystemConfigPageResponse>> page(@ParameterObject SystemConfigPageQuery query) {
        return ApiResponse.success(systemConfigService.pageConfigs(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:config:query")
    @Operation(summary = "Get system config detail")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "System config detail loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "System config not found",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":404,\"message\":\"System config not found\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        )
    })
    public ApiResponse<SystemConfigDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(systemConfigService.getConfig(id));
    }

    @PostMapping
    @RequirePermission("system:config:create")
    @Operation(summary = "Create system config")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "System config created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"configKey must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "System config key already exists",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SYSTEM_CONFIG_KEY_ALREADY_EXISTS))
        )
    })
    public ApiResponse<Long> create(@Valid @RequestBody SystemConfigSaveRequest request) {
        return ApiResponse.success(systemConfigService.createConfig(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:config:update")
    @Operation(summary = "Update system config")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody SystemConfigSaveRequest request) {
        systemConfigService.updateConfig(id, request);
        return ApiResponse.success(true);
    }
}
