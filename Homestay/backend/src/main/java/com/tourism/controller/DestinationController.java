package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.entity.Destination;
import com.tourism.security.Public;
import com.tourism.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 目的地(游客公开浏览)
 */
@RestController
@RequestMapping("/api/destination")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    /** 全部展示目的地 */
    @Public
    @GetMapping("/list")
    public Result<List<Destination>> list() {
        return Result.success(destinationService.listShown());
    }

    /** 热门目的地(首页用) */
    @Public
    @GetMapping("/hot")
    public Result<List<Destination>> hot(Integer limit) {
        return Result.success(destinationService.hotDestinations(limit == null ? 6 : limit));
    }

    /** 目的地详情 */
    @Public
    @GetMapping("/{id}")
    public Result<Destination> detail(@PathVariable Long id) {
        return Result.success(destinationService.detail(id));
    }
}
