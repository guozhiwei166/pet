package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.entity.TourismExperience;
import com.tourism.security.Public;
import com.tourism.service.TourismExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 旅游体验接口(游客公开)
 */
@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
public class TourismExperienceController {

    private final TourismExperienceService experienceService;

    @Public
    @GetMapping("/page")
    public Result<PageVO<TourismExperience>> page(ResourceQueryDTO query) {
        return Result.success(experienceService.page(query));
    }

    @Public
    @GetMapping("/{id}")
    public Result<TourismExperience> detail(@PathVariable Long id) {
        return Result.success(experienceService.detail(id));
    }

    /** 民宿关联的体验 */
    @Public
    @GetMapping("/by-homestay/{homestayId}")
    public Result<List<TourismExperience>> byHomestay(@PathVariable Long homestayId) {
        return Result.success(experienceService.listByHomestay(homestayId));
    }
}
