package com.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.entity.Restaurant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RestaurantMapper extends BaseMapper<Restaurant> {
}
