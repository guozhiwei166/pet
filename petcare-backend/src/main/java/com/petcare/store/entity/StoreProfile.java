package com.petcare.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 门店信息（公开页展示） */
@Data
@TableName("store_profile")
public class StoreProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String storeName;
    private String slogan;
    private String intro;
    private String address;
    private String phone;
    private String openHours;
    private String features;
    private String photos;
    private LocalDateTime updateTime;
}
