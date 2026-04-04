package com.luckycolor.admin.common.config;

public final class OpenApiExamplePayloads {

    public static final String UNAUTHORIZED =
        "{\"code\":401,\"message\":\"未登录或登录已失效\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}";

    public static final String FORBIDDEN =
        "{\"code\":403,\"message\":\"无权限访问\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}";

    public static final String REQUEST_PARAMETER_INVALID =
        "{\"code\":400,\"message\":\"请求参数不合法\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}";

    public static final String FILE_NOT_FOUND =
        "{\"code\":404,\"message\":\"文件不存在\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}";

    public static final String USERNAME_ALREADY_EXISTS =
        "{\"code\":409,\"message\":\"用户名已存在\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}";

    public static final String SYSTEM_CONFIG_KEY_ALREADY_EXISTS =
        "{\"code\":409,\"message\":\"系统参数键已存在\",\"data\":null,\"timestamp\":\"2026-04-02T03:20:55.744567200Z\"}";

    private OpenApiExamplePayloads() {
    }
}
