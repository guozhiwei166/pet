package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价 VO
 */
@Data
public class ReviewVO {

    private Long id;
    private Long orderId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long homestayId;
    private Integer rating;
    private Integer hygieneScore;
    private Integer serviceScore;
    private Integer facilityScore;
    private Integer locationScore;
    private String content;
    private List<String> images;
    private String merchantReply;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 民宿总评分(列表项附带) */
    private java.math.BigDecimal homestayRating;
    /** 订单房型(评价上下文) */
    private String roomTypeName;
}
