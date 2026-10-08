package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.MerchantProfileDTO;
import com.tourism.entity.Merchant;
import com.tourism.security.LoginUser;
import com.tourism.security.Public;
import com.tourism.service.MerchantService;
import com.tourism.service.ReviewService;
import com.tourism.vo.MerchantPublicVO;
import com.tourism.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家中心接口(商家经营侧):资料 + 评价;公开主页供游客浏览
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantCenterController {

    private final MerchantService merchantService;
    private final ReviewService reviewService;

    /** 商家公开主页(无需登录) */
    @Public
    @GetMapping("/public/{id}")
    public Result<MerchantPublicVO> publicDetail(@PathVariable Long id) {
        return Result.success(merchantService.publicDetail(id));
    }

    /** 商家资料(当前登录商家) */
    @GetMapping("/profile")
    public Result<Merchant> profile() {
        LoginUser.requireRole("MERCHANT");
        return Result.success(merchantService.currentMerchant());
    }

    /** 更新商家资料 */
    @PutMapping("/profile")
    public Result<Merchant> updateProfile(@Valid @RequestBody MerchantProfileDTO dto) {
        LoginUser.requireRole("MERCHANT");
        return Result.success(merchantService.updateProfile(dto));
    }

    /** 商家查看自己民宿的评价 */
    @GetMapping("/reviews")
    public Result<PageVO<ReviewVO>> reviews(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "5") int size,
                                            @RequestParam(required = false) Long homestayId) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        return Result.success(reviewService.pageByMerchant(
                merchantService.currentMerchant().getId(), current, size, homestayId));
    }
}
