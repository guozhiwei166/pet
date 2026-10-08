package com.pawstay.interceptor;

import com.pawstay.util.JwtUtil;
import com.pawstay.util.LoginUser;
import com.pawstay.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 登录鉴权拦截器：公开接口白名单放行，其余校验 Bearer Token 并注入 UserContext
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/auth/")) return true;
        if ("GET".equalsIgnoreCase(method) && (path.startsWith("/api/home")
                || path.startsWith("/api/merchants/")
                || path.startsWith("/api/reviews"))) return true;

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return unauthorized(response);
        try {
            Map<String, Object> payload = JwtUtil.parse(auth.substring(7));
            UserContext.set(LoginUser.of(Long.valueOf(payload.get("uid").toString()), (String) payload.get("role")));
            return true;
        } catch (Exception e) {
            return unauthorized(response);
        }
    }

    private boolean unauthorized(HttpServletResponse response) throws java.io.IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
