package com.luckycolor.admin.common.error;

public final class ApiErrorCode {

    public static final int SUCCESS = 200;

    public static final int AUTH_LOGIN_FAILED = 1011001;

    public static final int AUTH_ACCOUNT_DISABLED = 1011002;

    public static final int AUTH_CAPTCHA_INVALID = 1011004;

    public static final int AUTH_CAPTCHA_TOKEN_INVALID = 1011005;

    public static final int AUTH_TOKEN_EXPIRED = 1011007;

    public static final int AUTH_TOKEN_INVALID = 1011008;

    public static final int AUTH_REFRESH_TOKEN_EXPIRED = 1011009;

    public static final int AUTH_REFRESH_TOKEN_INVALID = 1011010;

    public static final int AUTH_LOGIN_LOCKED = 1011011;

    public static final int AUTH_CAPTCHA_RATE_LIMITED = 1011012;

    public static final int AUTH_REFRESH_RATE_LIMITED = 1011013;

    public static final int PERMISSION_DENIED = 1012001;

    public static final int BAD_REQUEST = 40000;

    public static final int UNAUTHORIZED = 40100;

    public static final int FORBIDDEN = 40300;

    public static final int NOT_FOUND = 40400;

    public static final int CONFLICT = 40900;

    public static final int TOO_MANY_REQUESTS = 42900;

    public static final int INTERNAL_SERVER_ERROR = 50000;

    private ApiErrorCode() {
    }
}
