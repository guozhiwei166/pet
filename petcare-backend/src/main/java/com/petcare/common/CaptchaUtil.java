package com.petcare.common;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 图形验证码：4 位字符（去除易混淆字符），Base64 PNG 输出
 */
public class CaptchaUtil {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String randomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }

    /** 返回 dataURL 形式的 PNG 验证码图片 */
    public static String toDataUrl(String code) {
        int w = 120, h = 42;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        // 底色
        g.setColor(new Color(0xEA, 0xF2, 0xED));
        g.fillRect(0, 0, w, h);
        // 干扰线
        g.setStroke(new BasicStroke(1.4f));
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(140 + RANDOM.nextInt(80), 180 + RANDOM.nextInt(50), 150 + RANDOM.nextInt(60)));
            g.drawLine(RANDOM.nextInt(w), RANDOM.nextInt(h), RANDOM.nextInt(w), RANDOM.nextInt(h));
        }
        // 字符
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(60 + RANDOM.nextInt(60), 110 + RANDOM.nextInt(50), 90 + RANDOM.nextInt(40)));
            g.drawString(String.valueOf(code.charAt(i)), 12 + i * 26, 30 + RANDOM.nextInt(6) - 3);
        }
        g.dispose();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }
}
