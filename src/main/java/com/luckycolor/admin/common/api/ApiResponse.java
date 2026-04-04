package com.luckycolor.admin.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.luckycolor.admin.common.error.ApiErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Standard API response envelope")
public record ApiResponse<T>(
    @Schema(description = "Business status code", example = "200")
    int code,
    @Schema(description = "Response message", example = "ok")
    String message,
    @Schema(description = "Business payload")
    T data,
    @Schema(description = "Response timestamp in UTC", example = "2026-04-02T03:20:55.744567200Z")
    Instant timestamp
) {

    @JsonProperty("msg")
    public String msg() {
        return message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiErrorCode.SUCCESS, "ok", data, Instant.now());
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now());
    }
}
