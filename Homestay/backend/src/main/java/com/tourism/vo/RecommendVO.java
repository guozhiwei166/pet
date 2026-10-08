package com.tourism.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐民宿 VO(含可解释因子)
 */
@Data
public class RecommendVO {

    private Long id;
    private String name;
    private String coverImage;
    private String intro;
    private String destinationName;
    private BigDecimal minPrice;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer favoriteCount;
    private String features;

    /** 综合推荐分 0-100 */
    private BigDecimal score;

    /** 各因子得分与解释(前端展示"为什么推荐") */
    private List<Factor> factors;

    @Data
    public static class Factor {
        private String name;       // 因子名,如 用户评分
        private String description;// 描述
        private BigDecimal weight; // 权重
        private BigDecimal score;  // 该项得分 0-100
        private String explain;    // 如 "评分 4.8/5 → 96 分"
    }
}
