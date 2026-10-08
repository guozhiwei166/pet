package com.pawstay.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawstay.common.BusinessException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 轻量级 JWT 实现（HS256 签名），无第三方依赖，便于教学与答辩讲解
 */
public class JwtUtil {
    private static final String SECRET = "pawstay-jwt-secret-2026";
    private static final long EXPIRE_MS = 7L * 24 * 3600 * 1000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String create(Long uid, String role) {
        try {
            String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            Map<String, Object> payload = new HashMap<>();
            payload.put("uid", uid);
            payload.put("role", role);
            payload.put("exp", System.currentTimeMillis() + EXPIRE_MS);
            String body = b64(MAPPER.writeValueAsBytes(payload));
            String sign = b64(hmac((header + "." + body).getBytes(StandardCharsets.UTF_8)));
            return header + "." + body + "." + sign;
        } catch (Exception e) {
            throw new RuntimeException("生成令牌失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("格式错误");
            String expect = b64(hmac((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8)));
            if (!expect.equals(parts[2])) throw new IllegalArgumentException("签名不匹配");
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

    private static byte[] hmac(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static String b64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
