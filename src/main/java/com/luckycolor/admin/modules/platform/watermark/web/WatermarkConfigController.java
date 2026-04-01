package com.luckycolor.admin.modules.platform.watermark.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.watermark.mapper.WatermarkConfigMapper;
import com.luckycolor.admin.modules.platform.watermark.service.WatermarkConfigService;
import com.luckycolor.admin.modules.platform.watermark.web.request.WatermarkConfigSaveRequest;
import com.luckycolor.admin.modules.platform.watermark.web.response.WatermarkConfigResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(WatermarkConfigMapper.class)
public class WatermarkConfigController {

    private final WatermarkConfigService watermarkConfigService;

    public WatermarkConfigController(WatermarkConfigService watermarkConfigService) {
        this.watermarkConfigService = watermarkConfigService;
    }

    @GetMapping
    @RequirePermission("watermark:query")
    public ApiResponse<WatermarkConfigResponse> getCurrent(Authentication authentication) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        return ApiResponse.success(watermarkConfigService.getCurrentConfig(authenticatedUser.tenantId()));
    }

    @PutMapping
    @RequirePermission("watermark:update")
    public ApiResponse<Boolean> saveCurrent(
        Authentication authentication,
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
