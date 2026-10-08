package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.ReviewCreateDTO;
import com.tourism.dto.ReviewReplyDTO;
import com.tourism.security.LoginUser;
import com.tourism.security.Public;
import com.tourism.service.MerchantService;
import com.tourism.service.ReviewService;
import com.tourism.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价接口
 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MerchantService merchantService;

    /** 民宿评价列表(公开) */
    @Public
    @GetMapping("/homestay/{homestayId}")
    public Result<PageVO<ReviewVO>> byHomestay(@PathVariable Long homestayId,
                                               @RequestParam(defaultValue = "1") int current,
                                               @RequestParam(defaultValue = "5") int size) {
        return Result.success(reviewService.pageByHomestay(homestayId, current, size));
    }

    /** 发表评价(用户) */
    @PostMapping("/create")
    public Result<ReviewVO> create(@Valid @RequestBody ReviewCreateDTO dto) {
        LoginUser.requireRole("USER");
        return Result.success(reviewService.create(dto, LoginUser.userId()));
    }

    /** 我的评价 */
    @GetMapping("/my")
    public Result<PageVO<ReviewVO>> my(@RequestParam(defaultValue = "1") int current,
                                       @RequestParam(defaultValue = "5") int size) {
        return Result.success(reviewService.pageByUser(LoginUser.userId(), current, size));
    }

    /** 商家回复评价 */
    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @Valid @RequestBody ReviewReplyDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        reviewService.reply(id, merchantService.currentMerchant().getId(), dto.getReply());
        return Result.success();
    }
}
