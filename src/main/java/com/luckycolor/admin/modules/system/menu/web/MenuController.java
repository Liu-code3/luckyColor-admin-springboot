package com.luckycolor.admin.modules.system.menu.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.menu.service.MenuService;
import com.luckycolor.admin.modules.system.menu.web.request.MenuSaveRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuStatusRequest;
import com.luckycolor.admin.modules.system.menu.web.request.MenuTreeQuery;
import com.luckycolor.admin.modules.system.menu.web.response.MenuDetailResponse;
import com.luckycolor.admin.modules.system.menu.web.response.MenuTreeResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/menus")
@ConditionalOnPersistenceEnabled
@Validated
@Tag(name = "System Menus", description = "System menu management APIs")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    @RequirePermission("system:menu:query")
    @Operation(summary = "List system menu tree")
    public ApiResponse<List<MenuTreeResponse>> tree(@ParameterObject MenuTreeQuery query) {
        return ApiResponse.success(menuService.listMenuTree(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:menu:query")
    @Operation(summary = "Get system menu detail")
    public ApiResponse<MenuDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.success(menuService.getMenu(id));
    }

    @PostMapping
    @RequirePermission("system:menu:create")
    @Operation(summary = "Create system menu")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Menu created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"menuName must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
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
    public ApiResponse<Long> create(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(menuService.createMenu(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:update")
    @Operation(summary = "Update system menu")
    public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        menuService.updateMenu(id, request);
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:menu:update")
    @Operation(summary = "Update system menu status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody MenuStatusRequest request) {
        menuService.updateMenuStatus(id, request);
        return ApiResponse.success(true);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    @Operation(summary = "Delete system menu")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Menu deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success(true);
    }
}
