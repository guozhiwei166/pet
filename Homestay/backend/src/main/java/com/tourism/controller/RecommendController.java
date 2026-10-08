package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.security.LoginUser;
import com.tourism.security.Public;
import com.tourism.service.RecommendService;
import com.tourism.vo.RecommendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 民宿推荐接口(创新点③,游客/用户均可访问)
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /** 推荐民宿列表(登录用户自动融合偏好;游客按默认参考点) */
    @Public
    @GetMapping("/homestays")
    public Result<List<RecommendVO>> homestays(@RequestParam(defaultValue = "6") int limit,
                                               @RequestParam(required = false) Long destinationId,
                                               @RequestParam(required = false) BigDecimal maxPrice) {
        Long userId = LoginUser.userId(); // 匿名时为 null(公开路径带 token 才写入上下文)
        return Result.success(recommendService.recommend(limit, destinationId, userId, maxPrice));
    }
}
