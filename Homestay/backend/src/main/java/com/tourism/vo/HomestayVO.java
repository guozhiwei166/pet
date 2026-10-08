package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 民宿详情 VO(含图集、房型、评分分布由前端另行拉取)
 */
@Data
public class HomestayVO {

    private Long id;
    private Long merchantId;
    private Long destinationId;
    private String destinationName;
    private String merchantName;
    private String name;
    private String coverImage;
    private List<String> images;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String intro;
    private String description;
    private String features;
    private String facilities;
    private String phone;
    private String businessStatus;
    private String auditStatus;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private BigDecimal minPrice;
    /** 当前登录用户是否已收藏(渐进增强) */
    private Boolean favorited;
    /** 商家视角:房型数 / 近30天订单数(列表摘要用) */
    private Integer roomTypeCount;
    private Long monthOrderCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
