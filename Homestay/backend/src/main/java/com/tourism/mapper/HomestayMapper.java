package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.Homestay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface HomestayMapper extends BaseMapper<Homestay> {

    /** 原子浏览量自增(避免并发丢计数) */
    @Update("UPDATE homestay SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int incrementView(@Param("id") Long id);
}
