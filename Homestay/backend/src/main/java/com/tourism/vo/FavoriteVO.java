package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏列表项 VO
 */
@Data
public class FavoriteVO {

    private Long favoriteId;
    private Long targetId;
    /** 1民宿 2景点 3体验 4餐饮 */
    private Integer targetType;
    private String typeText;

    private String name;
    private String coverImage;
    private BigDecimal rating;
    private BigDecimal price;
    private String destinationName;
    private String intro;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime favoriteTime;
}
