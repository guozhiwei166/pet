package com.tourism.common;

/**
 * 业务状态码与提示文案统一收口,避免魔法值散落
 */
public interface ResultCode {

    int SUCCESS = 200;
    // 参数/业务/权限/未认证
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;   // 未登录 / 登录过期
    int FORBIDDEN = 403;      // 已登录但无权限
    int NOT_FOUND = 404;
    int CONFLICT = 409;       // 业务冲突,如重复操作/状态不允许
    int ERROR = 500;

    String MSG_SUCCESS = "操作成功";
}
