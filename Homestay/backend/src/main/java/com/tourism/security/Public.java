package com.tourism.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公开访问注解:标注在 Controller 方法(或类)上表示无需登录即可访问。
 * 安全模型:默认所有 /api/** 均需登录;仅显式标注 @Public 才公开。
 * 仅用于纯浏览类 GET 接口,写接口一律不允许标注。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Public {
}
