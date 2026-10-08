package com.petcare.common;

import lombok.Data;

/**
 * 统一响应结构：code=200 成功，400 业务错误，401 未登录，403 无权限，500 系统错误
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> Result<T> ok() { return build(200, "success", null); }
    public static <T> Result<T> ok(T data) { return build(200, "success", data); }
    public static <T> Result<T> error(int code, String message) { return build(code, message, null); }

    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = data;
        return r;
    }
}
