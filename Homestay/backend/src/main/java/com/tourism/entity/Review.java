package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 民宿评价
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review")
public class Review extends BaseEntity {

    private Long orderId;
    private Long userId;
    private Long homestayId;
    private Integer rating;
    private Integer hygieneScore;
    private Integer serviceScore;
    private Integer facilityScore;
    private Integer locationScore;
    private String content;
    /** 评价图片 JSON 数组 */
    private String images;
    private String merchantReply;
    private java.time.LocalDateTime replyTime;
    /** 0=正常 1=管理员隐藏 */
    private Integer status;
}
