package com.petcare.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long petId;
    private Integer ratingOverall;
    private Integer ratingEnv;
    private Integer ratingService;
    private Integer ratingStaff;
    private Integer ratingPetStatus;
    private String content;
    private String tags;
    private String reply;
    private LocalDateTime replyTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
