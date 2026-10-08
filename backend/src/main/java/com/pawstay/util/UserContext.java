package com.pawstay.util;

import com.pawstay.common.BusinessException;

/**
 * 基于 ThreadLocal 的当前登录用户上下文，由 AuthInterceptor 写入、请求结束时清理
 */
public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) { HOLDER.set(user); }

    public static LoginUser get() { return HOLDER.get(); }

    public static LoginUser requireLogin() {
        LoginUser u = HOLDER.get();
        if (u == null) throw new BusinessException(401, "请先登录");
        return u;
    }

    public static void requireRole(String role) {
        requireLogin();
        if (!role.equals(HOLDER.get().getRole())) throw new BusinessException(403, "没有权限执行此操作");
    }

    public static void clear() { HOLDER.remove(); }
}
