package com.petcare.security;

import com.petcare.common.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户读取工具（JWT 过滤器已写入 SecurityContext）
 */
public class SecurityUtils {

    public static LoginUser current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        throw new BusinessException(401, "请先登录");
    }

    public static Long currentId() {
        return current().getId();
    }

    public static String currentRole() {
        return current().getRole();
    }

    public static void requireRole(String role) {
        if (!role.equals(currentRole())) {
            throw new BusinessException(403, "没有权限执行此操作");
        }
    }
}
