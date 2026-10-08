package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.dto.ReviewDTO;
import com.pawstay.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 评价：主人评价、商家回复、公开查看 */
@RestController
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/api/reviews")
    public Result<List<Map<String, Object>>> list(@RequestParam Long merchantId,
                                                  @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(reviewService.listByMerchant(merchantId, limit));
    }

    @PostMapping("/api/orders/{id}/review")
    public Result<Void> create(@PathVariable Long id, @RequestBody ReviewDTO dto) {
        reviewService.create(id, dto);
        return Result.ok();
    }

    @PostMapping("/api/reviews/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reviewService.reply(id, body.get("reply"));
        return Result.ok();
    }
}
