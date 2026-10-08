package com.petcare.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 轻量级 JWT（HS256）。载荷：uid / username / role / exp
 */
@Component
public class JwtUtil {

    @Value("${petcare.jwt.secret}")
    private String secret;

    @Value("${petcare.jwt.expire-hours}")
    private long expireHours;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String create(Long uid, String username, String role) {
        try {
            String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            Map<String, Object> payload = new HashMap<>();
            payload.put("uid", uid);
            payload.put("username", username);
            payload.put("role", role);
            payload.put("exp", System.currentTimeMillis() + expireHours * 3600_000L);
            String body = b64(MAPPER.writeValueAsBytes(payload));
            return header + "." + body + "." + b64(hmac((header + "." + body).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("生成令牌失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException();
            String expect = b64(hmac((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8)));
            // 恒定时间比较，防签名时序攻击
            byte[] expected = expect.getBytes(StandardCharsets.UTF_8);
            byte[] actual = parts[2].getBytes(StandardCharsets.UTF_8);
            if (!java.security.MessageDigest.isEqual(expected, actual)) {
                throw new IllegalArgumentException("签名不匹配");
            }
            Map<String, Object> payload = MAPPER.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            if (System.currentTimeMillis() > ((Number) payload.get("exp")).longValue()) {
                throw new IllegalArgumentException("令牌过期");
            }
            return payload;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
    }

    /** 剩余有效期毫秒数（用于登出黑名单 TTL） */
    public long remainingMs(String token) {
        Map<String, Object> payload = parse(token);
        return Math.max(0, ((Number) payload.get("exp")).longValue() - System.currentTimeMillis());
    }

    public Long uid(Map<String, Object> payload) {
        return Long.valueOf(payload.get("uid").toString());
    }

    public String role(Map<String, Object> payload) {
        return (String) payload.get("role");
    }

    public String username(Map<String, Object> payload) {
        return (String) payload.get("username");
    }

    private byte[] hmac(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data);
    }

    private String b64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
