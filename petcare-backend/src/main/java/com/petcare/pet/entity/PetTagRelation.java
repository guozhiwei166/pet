package com.petcare.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pet_tag_relation")
public class PetTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long tagId;
}
