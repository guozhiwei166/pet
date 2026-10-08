package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 举报
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report")
public class Report extends BaseEntity {

    /** 1=民宿 2=评价 3=体验 4=餐饮 */
    private Integer reportType;
    private Long targetId;
    private String reason;
    private String description;
    private Long reporterId;
    /** PENDING / HANDLING / HANDLED / REJECTED */
    private String status;
    private LocalDateTime handleTime;
    private String handleRemark;
}
