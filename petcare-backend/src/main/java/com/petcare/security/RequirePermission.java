package com.petcare.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 细粒度权限校验：校验当前用户角色在 sys_role.permissions 中声明的权限码。
 * - value 支持逗号分隔多个权限码，命中任一即放行（OR 语义）
 * - 权限码支持 "*" 通配，如 "room:*" 可匹配 "room:manage"/"room:status"
 * - ADMIN 角色 permissions='*'，天然放行一切
 * 与 Spring Security 的角色注解（@PreAuthorize）互补：角色管"能不能进模块"，权限码管"具体操作"。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String[] value();
}
