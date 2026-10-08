package com.petcare.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 请求体（Map&lt;String,Object&gt;）字段取值工具。
 * 客户端传参类型/格式不符时抛 400 业务异常，避免 ClassCastException、NumberFormatException
 * 直接冒泡成 500，让前端拿到可读的提示而不是"系统开小差"。
 */
public final class Params {

    private Params() {
    }

    /** 字符串字段：null 与空白串统一返回 null；非字符串按 toString 取值 */
    public static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    /** 必填长整型字段 */
    public static Long requireLong(Map<String, Object> body, String key, String label) {
        String s = str(body, key);
        if (s == null) throw new BusinessException("请填写" + label);
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    /** 可空长整型字段 */
    public static Long longOrNull(Map<String, Object> body, String key, String label) {
        String s = str(body, key);
        if (s == null) return null;
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    /** 可空整型字段 */
    public static Integer intOrNull(Map<String, Object> body, String key, String label) {
        String s = str(body, key);
        if (s == null) return null;
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    /** 可空小数字段（体重、体温等） */
    public static BigDecimal decimalOrNull(Map<String, Object> body, String key, String label) {
        String s = str(body, key);
        if (s == null) return null;
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    /** 必填日期字段（yyyy-MM-dd） */
    public static LocalDate requireDate(Map<String, Object> body, String key, String label) {
        String s = str(body, key);
        if (s == null) throw new BusinessException("请选择" + label);
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new BusinessException(label + "格式不正确（应为 yyyy-MM-dd）");
        }
    }
}
