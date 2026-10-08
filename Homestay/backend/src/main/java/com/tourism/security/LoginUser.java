package com.tourism.security;

import com.tourism.common.BizException;
import com.tourism.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文(ThreadLocal):由 JwtInterceptor 写入,Service/Controller 取用
 */
public class LoginUser {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
    }

    public static void set(UserInfo info) {
        HOLDER.set(info);
    }

    public static UserInfo get() {
        return HOLDER.get();
    }

    public static Long userId() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.getUserId();
    }

    public static String role() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.getRole();
    }

    /** 必须已登录,否则抛 401 */
    public static UserInfo require() {
        UserInfo info = HOLDER.get();
        if (info == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        return info;
    }

    /** 必须是指定角色之一,否则抛 403 */
    public static void requireRole(String... roles) {
        UserInfo info = require();
        for (String role : roles) {
            if (role.equals(info.getRole())) {
                return;
            }
        }
        throw new BizException(ResultCode.FORBIDDEN, "无权访问该资源");
    }

    public static void clear() {
        HOLDER.remove();
    }
}
