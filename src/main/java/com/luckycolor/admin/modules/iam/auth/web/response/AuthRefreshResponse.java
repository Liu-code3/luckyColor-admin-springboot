package com.luckycolor.admin.modules.iam.auth.web.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh token result payload")
public record AuthRefreshResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.demo.signature")
    String accessToken,
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,
    @Schema(description = "Token expiration in seconds", example = "7200")
    long expiresIn,
    @JsonIgnore
    String refreshToken
) {
}
