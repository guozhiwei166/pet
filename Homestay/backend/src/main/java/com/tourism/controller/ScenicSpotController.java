package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.ScenicSpot;
import com.tourism.security.Public;
import com.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 景点接口(游客公开)
 */
@RestController
@RequestMapping("/api/scenic")
@RequiredArgsConstructor
public class ScenicSpotController {

    private final ScenicSpotService scenicSpotService;

    @Public
    @GetMapping("/page")
    public Result<PageVO<ScenicSpot>> page(ResourceQueryDTO query) {
        return Result.success(scenicSpotService.page(query));
    }

    @Public
    @GetMapping("/{id}")
    public Result<ScenicSpot> detail(@PathVariable Long id) {
        return Result.success(scenicSpotService.detail(id));
    }

    /** 民宿关联的景点 */
    @Public
    @GetMapping("/by-homestay/{homestayId}")
    public Result<List<ScenicSpot>> byHomestay(@PathVariable Long homestayId) {
        return Result.success(scenicSpotService.listByHomestay(homestayId));
    }
}
