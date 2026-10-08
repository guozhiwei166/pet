package com.tourism.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.common.Result;
import com.tourism.common.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * JWT 认证 + 角色授权拦截器。
 * 安全模型:所有 /api/** 默认需登录;Controller 方法上显式标注 @Public 才公开浏览;
 * @RequireRole 标注在方法/类上做角色授权(方法优先于类)。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private boolean isApiRequest(String uri) {
        return uri.startsWith("/api/");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 预检直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();

        if (!isApiRequest(uri)) {
            // 静态资源/上传文件等:若带合法 token 仍写入上下文(渐进增强)
            attachIfTokenValid(request);
            return true;
        }

        HandlerMethod hm = (handler instanceof HandlerMethod) ? (HandlerMethod) handler : null;
        boolean isPublic = hm != null && (hm.hasMethodAnnotation(Public.class)
                || hm.getBeanType().isAnnotationPresent(Public.class));

        String token = resolveToken(request);
        Claims claims = null;
        if (StringUtils.hasText(token)) {
            claims = jwtUtil.parse(token);
        }
        if (claims != null) {
            LoginUser.set(new LoginUser.UserInfo(jwtUtil.getUserId(claims),
                    claims.get("username", String.class), jwtUtil.getRole(claims)));
        }

        if (isPublic) {
            return true; // 公开路径:有 token 则已写入上下文,无则匿名浏览
        }
        if (claims == null) {
            writeJson(response, ResultCode.UNAUTHORIZED, "请先登录");
            return false;
        }

        // 角色授权:方法注解优先,其次类注解
        RequireRole rr = hm == null ? null : hm.getMethodAnnotation(RequireRole.class);
        if (rr == null && hm != null) {
            rr = hm.getBeanType().getAnnotation(RequireRole.class);
        }
        if (rr != null) {
            boolean ok = false;
            for (String r : rr.value()) {
                if (r.equals(LoginUser.role())) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                writeJson(response, ResultCode.FORBIDDEN, "无权访问该资源");
                return false;
            }
        }
        return true;
    }

    private void attachIfTokenValid(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            Claims claims = jwtUtil.parse(token);
            if (claims != null) {
                LoginUser.set(new LoginUser.UserInfo(jwtUtil.getUserId(claims),
                        claims.get("username", String.class), jwtUtil.getRole(claims)));
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUser.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK); // 统一 HTTP 200,code 表达语义
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, msg)));
    }
}
