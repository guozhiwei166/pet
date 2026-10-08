package com.petcare.review.controller;

import com.petcare.common.OperationLog;
import com.petcare.common.Result;
import com.petcare.review.entity.Review;
import com.petcare.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/orders/{id}/review")
    @OperationLog(module = "评价", action = "发布评价")
    public Result<Void> create(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        reviewService.create(id, body);
        return Result.ok();
    }

    @PostMapping("/reviews/{id}/reply")
    @com.petcare.security.RequirePermission("review:reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reviewService.reply(id, body.get("reply"));
        return Result.ok();
    }

    @GetMapping("/pets/{petId}/reviews")
    public Result<List<Review>> listByPet(@PathVariable Long petId) {
        return Result.ok(reviewService.listByPet(petId));
    }

    /** 管理端评价总览（含全店评价，仅 STAFF/ADMIN 可见，避免主人信息泄露） */
    @GetMapping("/reviews")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public Result<List<Map<String, Object>>> listAll() {
        return Result.ok(reviewService.listAll());
    }
}
