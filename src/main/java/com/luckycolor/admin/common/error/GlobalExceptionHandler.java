package com.luckycolor.admin.common.error;

import com.luckycolor.admin.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure(ApiErrorCode.BAD_REQUEST, resolveValidationMessage(exception)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure(ApiErrorCode.BAD_REQUEST, "Request body is invalid"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure(ApiErrorCode.UNAUTHORIZED, "Unauthorized"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.failure(ApiErrorCode.FORBIDDEN, "Forbidden"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason();
        if (message == null || message.isBlank()) {
            message = status.getReasonPhrase();
        }
        return ResponseEntity.status(status)
            .body(ApiResponse.failure(resolveErrorCode(status, message), resolveMessage(status, message)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("unhandled application exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure(ApiErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"));
    }

    private String resolveValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return Objects.requireNonNull(methodArgumentNotValidException.getBindingResult().getFieldError()).getDefaultMessage();
        }
        if (exception instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                return fieldError.getDefaultMessage();
            }
        }
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Request parameter is invalid");
        }
        return "Request parameter is invalid";
    }

    private int resolveErrorCode(HttpStatus status, String reason) {
        return switch (reason) {
            case "AUTH_LOGIN_FAILED" -> ApiErrorCode.AUTH_LOGIN_FAILED;
            case "AUTH_ACCOUNT_DISABLED" -> ApiErrorCode.AUTH_ACCOUNT_DISABLED;
            case "AUTH_CAPTCHA_INVALID" -> ApiErrorCode.AUTH_CAPTCHA_INVALID;
            case "AUTH_CAPTCHA_TOKEN_INVALID" -> ApiErrorCode.AUTH_CAPTCHA_TOKEN_INVALID;
            case "AUTH_TOKEN_EXPIRED" -> ApiErrorCode.AUTH_TOKEN_EXPIRED;
            case "AUTH_TOKEN_INVALID", "TOKEN_INVALID", "TOKEN_REVOKED" -> ApiErrorCode.AUTH_TOKEN_INVALID;
            case "AUTH_REFRESH_TOKEN_EXPIRED" -> ApiErrorCode.AUTH_REFRESH_TOKEN_EXPIRED;
            case "AUTH_REFRESH_TOKEN_INVALID" -> ApiErrorCode.AUTH_REFRESH_TOKEN_INVALID;
            case "AUTH_LOGIN_LOCKED" -> ApiErrorCode.AUTH_LOGIN_LOCKED;
            case "AUTH_CAPTCHA_RATE_LIMITED" -> ApiErrorCode.AUTH_CAPTCHA_RATE_LIMITED;
            case "AUTH_REFRESH_RATE_LIMITED" -> ApiErrorCode.AUTH_REFRESH_RATE_LIMITED;
            case "PERMISSION_DENIED" -> ApiErrorCode.PERMISSION_DENIED;
            default -> switch (status) {
                case BAD_REQUEST -> ApiErrorCode.BAD_REQUEST;
                case UNAUTHORIZED -> ApiErrorCode.UNAUTHORIZED;
                case FORBIDDEN -> ApiErrorCode.FORBIDDEN;
                case TOO_MANY_REQUESTS -> ApiErrorCode.TOO_MANY_REQUESTS;
                case NOT_FOUND -> ApiErrorCode.NOT_FOUND;
                case CONFLICT -> ApiErrorCode.CONFLICT;
                default -> ApiErrorCode.INTERNAL_SERVER_ERROR;
            };
        };
    }

    private String resolveMessage(HttpStatus status, String reason) {
        return switch (reason) {
            case "AUTH_LOGIN_FAILED" -> "username or password is incorrect";
            case "AUTH_ACCOUNT_DISABLED" -> "account is disabled";
            case "AUTH_CAPTCHA_INVALID" -> "captcha answer is invalid or expired";
            case "AUTH_CAPTCHA_TOKEN_INVALID" -> "captcha token is invalid or expired";
            case "AUTH_TOKEN_EXPIRED" -> "access token expired, please sign in again";
            case "AUTH_TOKEN_INVALID", "TOKEN_INVALID", "TOKEN_REVOKED" -> "access token invalid, please sign in again";
            case "AUTH_REFRESH_TOKEN_EXPIRED" -> "refresh token expired, please sign in again";
            case "AUTH_REFRESH_TOKEN_INVALID" -> "refresh token invalid, please sign in again";
            case "AUTH_LOGIN_LOCKED" -> "too many failed login attempts, please try again later";
            case "AUTH_CAPTCHA_RATE_LIMITED" -> "captcha requests are too frequent, please try again later";
            case "AUTH_REFRESH_RATE_LIMITED" -> "refresh requests are too frequent, please try again later";
            case "PERMISSION_DENIED" -> "permission denied";
            default -> {
                if (reason != null && !reason.isBlank()) {
                    yield reason;
                }
                yield status.getReasonPhrase();
            }
        };
    }
}
