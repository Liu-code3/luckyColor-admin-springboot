package com.luckycolor.admin.modules.platform.watermark.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.watermark.mapper.WatermarkConfigMapper;
import com.luckycolor.admin.modules.platform.watermark.service.WatermarkConfigService;
import com.luckycolor.admin.modules.platform.watermark.web.request.WatermarkConfigSaveRequest;
import com.luckycolor.admin.modules.platform.watermark.web.response.WatermarkConfigResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/watermark-config/current")
@Validated
@ConditionalOnPersistenceEnabled
@Tag(name = "Watermark", description = "Watermark configuration APIs")
public class WatermarkConfigController {

    private final WatermarkConfigService watermarkConfigService;

    public WatermarkConfigController(WatermarkConfigService watermarkConfigService) {
        this.watermarkConfigService = watermarkConfigService;
    }

    @GetMapping
    @RequirePermission("watermark:query")
    @Operation(summary = "Get current watermark configuration")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current watermark configuration loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<WatermarkConfigResponse> getCurrent(@Parameter(hidden = true) Authentication authentication) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        return ApiResponse.success(watermarkConfigService.getCurrentConfig(authenticatedUser.tenantId()));
    }

    @PutMapping
    @RequirePermission("watermark:update")
    @Operation(summary = "Update current watermark configuration")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current watermark configuration updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"text must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Boolean> saveCurrent(
        @Parameter(hidden = true) Authentication authentication,
        @Valid @RequestBody WatermarkConfigSaveRequest request
    ) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        watermarkConfigService.saveCurrentConfig(authenticatedUser.tenantId(), request);
        return ApiResponse.success(true);
    }

    private JwtAuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }
}
