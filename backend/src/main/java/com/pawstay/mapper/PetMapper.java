package com.pawstay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pawstay.entity.Pet;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PetMapper extends BaseMapper<Pet> {

    @Select("SELECT species AS name, COUNT(*) AS cnt FROM pet_pet GROUP BY species")
    List<Map<String, Object>> selectSpeciesStats();
}
