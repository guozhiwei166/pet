package com.tourism.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限注解:标注允许访问的角色。不标注则默认需登录即可。
 * 例: @RequireRole(Constants.ROLE_ADMIN)  仅管理员;
 *     @RequireRole({ROLE_USER, ROLE_ADMIN}) 用户或管理员。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();
}
