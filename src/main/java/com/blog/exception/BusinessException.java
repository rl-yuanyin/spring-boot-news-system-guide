package com.blog.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 用于在 Service 层抛出，由 GlobalExceptionHandler 统一处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 快捷创建 400 错误
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 快捷创建 404 错误
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 快捷创建 403 错误
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 快捷创建 401 错误
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
}
