package com.pawstay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 寄养日记：商家每日护理打卡记录（喂食/遛弯/心情/照片），是本系统最具场景特色的数据
 */
@Data
@TableName("pet_care_record")
public class CareRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long merchantId;
    private LocalDate recordDate;
    private Integer mealDone;    // 1 已喂食
    private Integer walkDone;    // 1 已遛弯/放风
    private String mood;         // 开心 / 平静 / 想家 ...
    private String content;
    private String photos;       // Base64，多张逗号分隔
    private BigDecimal weight;   // 当日体重（用于健康曲线）
    private LocalDateTime createTime;
}
