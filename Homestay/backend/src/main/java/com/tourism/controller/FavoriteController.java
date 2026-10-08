package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.security.LoginUser;
import com.tourism.service.FavoriteService;
import com.tourism.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏接口(仅登录用户)
 */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏/取消收藏(切换) */
    @PostMapping("/toggle")
    public Result<Boolean> toggle(@RequestParam int targetType, @RequestParam Long targetId) {
        return Result.success(favoriteService.toggle(LoginUser.userId(), targetType, targetId));
    }

    /** 是否已收藏(供详情页判断) */
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam int targetType, @RequestParam Long targetId) {
        return Result.success(favoriteService.isFavorited(LoginUser.userId(), targetType, targetId));
    }

    /** 我的收藏列表 */
    @GetMapping("/list")
    public Result<PageVO<FavoriteVO>> list(@RequestParam(defaultValue = "1") int current,
                                           @RequestParam(defaultValue = "8") int size,
                                           @RequestParam(required = false) Integer targetType) {
        return Result.success(favoriteService.page(LoginUser.userId(), current, size, targetType));
    }

    /** 取消收藏(按目标,幂等) */
    @DeleteMapping("/{targetType}/{targetId}")
    public Result<Void> remove(@PathVariable int targetType, @PathVariable Long targetId) {
        favoriteService.remove(LoginUser.userId(), targetType, targetId);
        return Result.success();
    }
}
