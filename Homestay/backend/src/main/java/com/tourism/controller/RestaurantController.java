package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.Restaurant;
import com.tourism.security.Public;
import com.tourism.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 餐饮接口(游客公开)
 */
@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Public
    @GetMapping("/page")
    public Result<PageVO<Restaurant>> page(ResourceQueryDTO query) {
        return Result.success(restaurantService.page(query));
    }

    @Public
    @GetMapping("/{id}")
    public Result<Restaurant> detail(@PathVariable Long id) {
        return Result.success(restaurantService.detail(id));
    }

    /** 民宿关联的餐饮 */
    @Public
    @GetMapping("/by-homestay/{homestayId}")
    public Result<List<Restaurant>> byHomestay(@PathVariable Long homestayId) {
        return Result.success(restaurantService.listByHomestay(homestayId));
    }
}
