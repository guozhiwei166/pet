package com.pawstay.common;

import lombok.Data;

/**
 * 统一响应结构：code=0 成功，401 未登录，403 无权限，400 业务错误，500 系统错误
 */
@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() { return build(0, "success", null); }
    public static <T> Result<T> ok(T data) { return build(0, "success", data); }
    public static <T> Result<T> error(int code, String msg) { return build(code, msg, null); }

    private static <T> Result<T> build(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }
}
