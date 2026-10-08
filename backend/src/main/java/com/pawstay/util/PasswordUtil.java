package com.pawstay.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 密码散列：SHA-256(固定盐 + 原文)。生产环境建议 BCrypt，此处便于演示与讲解
 */
public class PasswordUtil {
    private static final String SALT = "pawstay";

    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((SALT + raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    public static boolean match(String raw, String hashed) {
        return hash(raw).equalsIgnoreCase(hashed);
    }
}
