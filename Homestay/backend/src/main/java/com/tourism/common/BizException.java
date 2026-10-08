package com.tourism.common;

/**
 * 业务异常:由 Service 主动抛出,GlobalExceptionHandler 统一转换为 Result
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.ERROR;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
