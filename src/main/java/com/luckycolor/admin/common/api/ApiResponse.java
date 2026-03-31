package com.luckycolor.admin.common.api;

import com.luckycolor.admin.common.error.ApiErrorCode;
import java.time.Instant;

public record ApiResponse<T>(int code, String message, T data, Instant timestamp) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiErrorCode.SUCCESS, "ok", data, Instant.now());
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now());
    }
}
