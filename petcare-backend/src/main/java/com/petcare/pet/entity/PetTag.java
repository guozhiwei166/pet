package com.petcare.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pet_tag")
public class PetTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer status;
}
