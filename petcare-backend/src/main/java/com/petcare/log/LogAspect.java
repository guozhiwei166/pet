package com.petcare.log;

import com.petcare.common.OperationLog;
import com.petcare.log.entity.OperationLogEntity;
import com.petcare.log.mapper.OperationLogMapper;
import com.petcare.security.LoginUser;
import com.petcare.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志切面：记录登录/退出/注册/改密/下单/入住/离店等关键动作
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String detail = null;
        Object result;
        try {
            result = joinPoint.proceed();
            detail = "成功";
            return result;
        } catch (Throwable e) {
            detail = "失败：" + e.getMessage();
            throw e;
        } finally {
            try {
                save(operationLog, detail, extractBody(joinPoint));
            } catch (Exception ignored) {
                // 日志失败不影响主流程
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String extractBody(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Map<?, ?> map && !map.isEmpty()) {
                return map.toString();
            }
        }
        return null;
    }

    private void save(OperationLog operationLog, String detail, Object body) {
        OperationLogEntity log = new OperationLogEntity();
        log.setModule(operationLog.module());
        log.setAction(operationLog.action());
        log.setDetail(detail != null && body != null ? detail + " " + body : detail);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
        } else {
            log.setUsername("anonymous");
        }

        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            log.setIp(ip == null ? request.getRemoteAddr() : ip.split(",")[0]);
            Object pathVars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (pathVars instanceof Map<?, ?> map && !map.isEmpty()) {
                log.setDetail((log.getDetail() == null ? "" : log.getDetail()) + " path=" + map);
            }
        }
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}
