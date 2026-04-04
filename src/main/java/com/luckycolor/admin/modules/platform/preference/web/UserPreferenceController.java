package com.luckycolor.admin.modules.platform.preference.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.platform.preference.mapper.UserPreferenceMapper;
import com.luckycolor.admin.modules.platform.preference.service.UserPreferenceService;
import com.luckycolor.admin.modules.platform.preference.web.request.UserPreferenceSaveRequest;
import com.luckycolor.admin.modules.platform.preference.web.response.UserPreferenceResponse;
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
@RequestMapping("/admin/user-preferences")
@Validated
@ConditionalOnPersistenceEnabled
@Tag(name = "User Preferences", description = "Current user preference APIs")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping("/current")
    @Operation(summary = "Get current user preferences")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user preferences loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        )
    })
    public ApiResponse<UserPreferenceResponse> getCurrent(@Parameter(hidden = true) Authentication authentication) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        return ApiResponse.success(
            userPreferenceService.getCurrentPreference(authenticatedUser.userId(), authenticatedUser.tenantId())
        );
    }

    @PutMapping("/current")
    @Operation(summary = "Update current user preferences")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user preferences updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\":400,\"message\":\"theme must not be blank\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}"))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        )
    })
    public ApiResponse<Boolean> saveCurrent(
        @Parameter(hidden = true) Authentication authentication,
        @Valid @RequestBody UserPreferenceSaveRequest request
    ) {
        JwtAuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        userPreferenceService.saveCurrentPreference(authenticatedUser.userId(), authenticatedUser.tenantId(), request);
        return ApiResponse.success(true);
    }

    private JwtAuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }
}
