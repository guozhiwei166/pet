package com.petcare.story.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 主人分享故事墙 */
@Data
@TableName("pet_story")
public class PetStory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long petId;
    private String title;
    private String content;
    private String photos;
    private Integer likes;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
